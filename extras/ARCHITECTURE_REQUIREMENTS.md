# Punchline — требования к архитектуре

Нормативный документ лабораторной работы № 1. Описывает Android-приложение
**Punchline** (коллекция шуток JokeAPI), обязанности компонентов и правила их
взаимодействия.

Связанные документы:

- [Описание проекта](PROJECT_DESCRIPTION.md)
- [Точные ограничения](CONSTRAINTS.md)
- [Практики проекта](Practices.md)

## 1. Архитектурная цель

Архитектура обеспечивает одновременно:

1. отдельную Activity для каждого экрана;
2. единый источник состояния для Compose UI и отсутствие бизнес-логики в
   Activity;
3. загрузку шуток из JokeAPI через абстракцию `JokeRepository` без сетевых типов
   в UI;
4. явные границы для профиля, настроек, кэша и резервной копии.

Используется прагматичный MVVM: Activity управляет навигацией и подпиской на
состояние, ViewModel выполняет сценарии, репозитории получают и сохраняют данные,
Compose отображает неизменяемое состояние.

## 2. Технологический базис

| Компонент | Выбор |
|---|---|
| Язык | Kotlin |
| UI | Jetpack Compose |
| UI-библиотека | Material Design 3 |
| Минимальная версия | API 24 |
| Сборка | Gradle Kotlin DSL и version catalog |
| Activity | `ComponentActivity` через общий `LoggedActivity` |
| Асинхронность | Coroutines, `StateFlow`, `Channel` |
| Сеть | `HttpURLConnection` из стандартной библиотеки |
| Профиль и настройки | `SharedPreferences` |
| Кэш и копия | JSON-файлы внутри `filesDir` |
| Тесты | JUnit для domain/data |

Отдельная HTTP-библиотека не нужна: текущий контракт JokeAPI прост, а
`HttpURLConnection` не добавляет зависимости и доступен на API 24. Сетевой вызов
выполняется в `Dispatchers.IO`; главный поток не блокируется.

## 3. Разделение ответственности

### 3.1. Activity

| Activity | Ответственность |
|---|---|
| `StartActivity` | брендинг, вход, запуск регистрации |
| `RegistrationActivity` | регистрация и возврат логина |
| `ContentActivity` | загрузка и показ коллекции шуток, переход к настройкам |
| `SettingsActivity` | профиль, оформление, управление копией |

Activity может извлечь навигационный параметр, создать ViewModel через фабрику,
вызвать `setContent`, обработать результат Activity Result и запустить или
закрыть следующую Activity.

Activity не должна читать JSON, выполнять HTTP-запросы, проверять пароль,
формировать карточки или выполнять файловые операции в главном потоке.

### 3.2. Compose UI

UI разделён на экран, повторно используемые компоненты и тему. Экран получает
`UiState` и функции-колбэки, не обращается к `Context`, `File`, HTTP или
репозиторию. Корневой composable подписывается на `StateFlow` через
`collectAsStateWithLifecycle`.

Целевое состояние ленты:

```kotlin
data class ContentUiState(
    val userName: String = "",
    val userAvatar: String = "laugh",
    val jokes: List<JokeCard> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: UiMessage? = null,
    val backupAvailable: Boolean = false
)
```

`JokeCard` — UI-модель. Сырой JSON JokeAPI в Compose не передаётся.

### 3.3. ViewModel

| Activity | ViewModel |
|---|---|
| `StartActivity` | `StartViewModel` |
| `RegistrationActivity` | `RegistrationViewModel` |
| `ContentActivity` | `ContentViewModel` |
| `SettingsActivity` | `SettingsViewModel` |

ViewModel принимает события UI, валидирует ввод, вызывает репозитории и
обновляет read-only `StateFlow`. Одноразовые сообщения и навигационные следствия
идут через буферизованный `Channel`. ViewModel не хранит `Activity`, `View`,
`Context` или `Compose`-типы и отменяет работу через `viewModelScope`.

### 3.4. Domain

Domain не импортирует Android, Compose, JSON или HTTP.

```kotlin
data class JokeCard(
    val id: Int,
    val category: JokeCategory,
    val type: JokeType,
    val setup: String,
    val punchline: String?,
    val safe: Boolean,
    val language: String,
    val wordCount: Int
)

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val error: ValidationError) : ValidationResult
}
```

`JokeCategory` включает `PROGRAMMING`, `MISC`, `DARK`, `PUN`, `SPOOKY`,
`CHRISTMAS` и `UNKNOWN`; `JokeType` — `SINGLE`, `TWOPART` и `UNKNOWN`.
Неизвестные категории и типы из API должны разбираться без падения приложения.

Валидация профиля проверяет непустые имя, логин, длину пароля, совпадение
подтверждения и возраст 6–120 лет. Одна функция проверяет одно правило; UI
показывает сообщение рядом с соответствующим полем.

### 3.5. Data

Основные контракты:

```kotlin
interface JokeRepository {
    suspend fun getJokes(): List<JokeCard>
    suspend fun refreshJokes(): List<JokeCard>
    suspend fun replaceJokes(jokes: List<JokeCard>)
    suspend fun clearCachedJokes()
}

interface BackupRepository {
    suspend fun createBackup(jokes: List<JokeCard>): BackupInfo
    suspend fun restoreBackup(): List<JokeCard>
    suspend fun deleteBackup()
    suspend fun getBackupInfo(): BackupInfo?
}
```

`RemoteJokeRepository`:

- выполняет запросы на `https://v2.jokeapi.dev/joke/Any`;
- передаёт `type=twopart`, `amount=10`, `safe-mode`, `lang=en`;
- выполняет до трёх пакетных запросов, пока не собрано 12 уникальных шуток;
- ограничивает ответ 20 шутками;
- преобразует элементы `jokes` в `JokeCard` через `JokeJsonCodec`;
- сохраняет успешный результат в `jokes_cache.json` атомарно;
- при ошибке не заменяет существующий кэш;
- не логирует полный ответ API, персональные данные или учётные данные.

`JsonBackupRepository` читает и записывает `jokes_backup.json` версии 2. Перед
заменой файла запись проверяется во временном файле. Копия содержит
`createdAt`, версию и массив шуток.

### 3.6. Storage

| Данные | Место хранения |
|---|---|
| профиль | `SharedPreferences` `punchline_user` |
| соль и хеш пароля | `SharedPreferences` `punchline_user` |
| тема и шрифт | `SharedPreferences` `punchline_settings` |
| кэш ленты | `filesDir/jokes_cache.json` |
| резервная копия | `filesDir/backup/jokes_backup.json` |
| флаг инициализации | `SharedPreferences` `joke_cache_state` |

Пароль не хранится в открытом виде. Хеширование использует HMAC-SHA256 с
уникальной солью и итерациями; массивы символов пароля очищаются после
использования. Соль и хеш никогда не передаются между Activity через `Intent`.

## 4. Потоки данных

### Авторизация

```text
UI → StartViewModel/RegistrationViewModel → Validation → UserRepository
→ SharedPreferences → StateFlow → StartActivity
```

### Загрузка шуток

```text
JokeAPI → HttpURLConnection → JSONObject → JokeJsonCodec
→ List<JokeCard> → RemoteJokeRepository (кэш) → ContentViewModel
→ StateFlow<ContentUiState> → LazyColumn → JokeCard composable
```

### Резервная копия

```text
SettingsScreen → SettingsViewModel → current List<JokeCard>
→ JsonBackupRepository → jokes_backup.json → BackupInfo → SettingsUiState
```

Одноразовые сообщения об успехе и ошибке не входят в долговременное UI-состояние:
повторная рекомпозиция и поворот экрана не должны повторять уведомление или
переход.

## 5. Навигационный контракт

```text
StartActivity ──«Создать аккаунт»──▶ RegistrationActivity
RegistrationActivity ──RESULT_OK(login)──▶ StartActivity
StartActivity ──успешный вход──▶ ContentActivity [finish StartActivity]
ContentActivity ──«Настройки»──▶ SettingsActivity [Activity Result]
SettingsActivity ──RESULT_OK(profileChanged)──▶ ContentActivity [finish SettingsActivity]
```

Через `Intent` передаётся только логин и признак изменения профиля. Пароль,
соль, хеш и полный профиль передавать нельзя. Повторный клик по навигационной
кнопке не создаёт дубликат Activity. Из настроек системная кнопка «Назад»
возвращает только к коллекции.

## 6. Жизненный цикл

Все Activity наследуют `LoggedActivity`. Переопределены `onCreate`, `onStart`,
`onRestart`, `onResume`, `onPause`, `onStop` и `onDestroy`. Сначала вызывается
`super`, затем выполняется запись:

```text
ContentActivity: onStart
```

Тег — `Lab1Lifecycle`. В журналах нет пароля, соли, хеша и полного содержимого
шуток. В отчёт включаются частичное перекрытие стартового экрана и полный
маршрут через четыре Activity.

## 7. Состояния и ошибки

| Ситуация | Поведение |
|---|---|
| первая загрузка | индикатор прогресса, запрос JokeAPI |
| успешная загрузка | 12–20 уникальных карточек и счётчик |
| сохранённый кэш | показ без обязательного сетевого запроса |
| ошибка сети или HTTP | сообщение и повторная загрузка |
| меньше 12 уникальных шуток | ошибка без перезаписи прежнего кэша |
| пустой кэш | состояние с восстановлением копии и переходом в настройки |
| отсутствие копии | восстановление и удаление неактивны |
| неверный ввод | текст ошибки у поля |
| ошибка записи копии | сообщение, старый файл по возможности сохраняется |

Сетевые исключения и ошибки JSON преобразуются в понятные сообщения не позднее
ViewModel. ViewModel не обязана разбирать исключения, если репозиторий уже
возвращает или выбрасывает сообщение, пригодное для UI. Приложение не должно
падать из-за сетевой ошибки API или повреждённого файла.

## 8. Тестируемость

Минимальные проверки без Android-эмулятора:

- границы имени, логина, пароля и возраста;
- политика объединения пакетов JokeAPI;
- дедупликация по `id` и ограничение ленты двадцатью шутками;
- достижение минимального размера ленты в 12 шуток;
- интеграционная проверка JSON на Android;
- отсутствие перезаписи кэша при неуспешной сетевой загрузке;
- уникальность `id` и ограничения размера коллекции;
- наличие сообщений об ошибках на экране.

Репозитории передаются в ViewModel через интерфейсы и могут быть заменены
фейками. `JokeJsonCodec` не должен зависеть от Activity.

## 9. Структура проекта

```text
com.example.a1lab
├── StartActivity.kt
├── RegistrationActivity.kt
├── ContentActivity.kt
├── SettingsActivity.kt
├── PunchlineApplication.kt
├── logging/LoggedActivity.kt
├── navigation/ActivityContracts.kt
├── domain/
│   ├── JokeCard.kt
│   ├── AppSettings.kt
│   ├── UserProfile.kt
│   └── Validation.kt
├── data/
│   ├── JokeRepository.kt
│   ├── RemoteJokeRepository.kt
│   ├── JokeJsonCodec.kt
│   ├── BackupRepository.kt
│   ├── JsonBackupRepository.kt
│   ├── SettingsRepository.kt
│   ├── UserRepository.kt
│   └── AppContainer.kt
├── storage/
│   ├── PasswordHasher.kt
│   ├── PreferenceSettingsRepository.kt
│   └── PreferenceUserRepository.kt
├── ui/
│   ├── start/
│   ├── registration/
│   ├── content/
│   ├── settings/
│   ├── components/
│   └── theme/
└── viewmodel/
    ├── StartViewModel.kt
    ├── RegistrationViewModel.kt
    ├── ContentViewModel.kt
    └── SettingsViewModel.kt
```

## 10. Критерии архитектурной приёмки

- [ ] В манифесте объявлены четыре Activity, LAUNCHER только у стартовой.
- [ ] UI не работает напрямую с HTTP, JSON, файлами и паролями.
- [ ] `JokeCard` не зависит от Android UI и формата ответа JokeAPI.
- [ ] `JokeRepository` позволяет заменить `RemoteJokeRepository` фейком.
- [ ] Сетевой запрос выполняется вне главного потока.
- [ ] Успешная загрузка сохраняется в кэш; ошибка не удаляет старые шутки.
- [ ] Копия шуток — отдельный JSON-файл с версией формата.
- [ ] Профиль, настройки, кэш и копия имеют явные места хранения.
- [ ] Пароли не сохраняются и не логируются в открытом виде.
- [ ] Все Activity наследуют `LoggedActivity` и пишут логи в нужном формате.
- [ ] Регистрация и настройки используют типизированные Activity Result.
- [ ] Domain и политика объединения пакетов JokeAPI покрыты unit-тестами.
