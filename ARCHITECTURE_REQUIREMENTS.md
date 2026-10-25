# Bookline — требования к архитектуре

Нормативный документ для лабораторной работы № 1 по дисциплине «Разработка
мобильных приложений». Описывает целевую структуру Android-приложения **Bookline**
(онлайн-библиотека), обязанности компонентов и правила взаимодействия между ними.

Связанные документы:

- [Описание проекта](PROJECT_DESCRIPTION.md)
- [Точные ограничения](CONSTRAINTS.md)
- [Исходное задание](<extras/Android. Лабораторная работа 1.pdf>)

## 1. Архитектурная цель

Архитектура должна обеспечить выполнение четырёх задач одновременно:

1. каждый пользовательский экран реализуется отдельной Activity;
2. состояние экранов и бизнес-логика не дублируются внутри Activity;
3. данные можно заменить с локального демонстрационного каталога на серверный
   источник без изменения Compose-экранов;
4. учётные данные, каталог и резервная копия имеют явные границы хранения.

Для ЛР 1 используется прагматичная многослойная архитектура: Activity отвечает
за композицию экрана и навигацию, Compose UI — за отображение состояния,
ViewModel — за операции конкретного экрана, репозитории — за получение и
сохранение данных.

## 2. Технологический базис

| Компонент | Обязательный выбор |
|---|---|
| Язык | Kotlin |
| UI | Jetpack Compose |
| UI-библиотека | Material Design 3 (`androidx.compose.material3`) |
| Минимальная версия | API 24 |
| Сборка | Gradle Kotlin DSL |
| Activity | `ComponentActivity` из AndroidX Activity Compose |
| Асинхронность | Kotlin Coroutines и `Flow`/`StateFlow` |
| Хранение профиля | DataStore Preferences либо `SharedPreferences` |
| Хранение каталога и копии | JSON-файл во внутреннем каталоге приложения |
| Тесты | JUnit для domain/data, Compose UI Test для ключевых экранов |

Версии библиотек задаются в `gradle/libs.versions.toml`, а не отдельными
литералами по коду. Новые зависимости добавляются только при наличии конкретной
задачи из требований.

## 3. Разделение ответственности

### 3.1. Activity

В приложении должны быть четыре Activity:

| Activity | Ответственность |
|---|---|
| `StartActivity` | стартовое состояние, вход, переход к регистрации |
| `RegistrationActivity` | регистрация и возврат результата |
| `ContentActivity` | каталог книг авторизованного пользователя |
| `SettingsActivity` | профиль, оформление и резервная копия |

Activity может:

- получить Intent и извлечь переданные параметры;
- создать ViewModel через фабрику;
- вызвать `setContent` и передать состояние в корневой composable;
- обработать системный результат, например `ActivityResult`;
- запустить следующую Activity;
- завершить себя методом `finish()` после успешного перехода.

Activity не должна:

- содержать бизнес-логику валидации;
- напрямую читать и записывать JSON-файл;
- хранить отдельные поля формы как источник истины;
- проверять пароль по незахешированному значению;
- формировать список карточек внутри разметки;
- выполнять сетевые или файловые операции в главном потоке;
- использовать Compose Navigation как замену четырём Activity.

### 3.2. Compose UI

Compose-слой делится на три вида компонентов:

1. **Экраны** — получают `UiState` и колбэки событий, не обращаются к
   репозиториям напрямую.
2. **Компоненты** — карточка книги, поле формы, пустое состояние, диалог
   подтверждения, индикатор загрузки.
3. **Тема** — цветовая схема, типографика и масштаб шрифта.

Экран должен быть stateless там, где это практически возможно. Корневой
composable подписывается на состояние ViewModel, а вложенные элементы не знают о
ViewModel. UI не должен самостоятельно преобразовывать DTO сетевого API в модель
интерфейса.

Имена ui-состояний:

```kotlin
data class ContentUiState(
    val userName: String = "",
    val books: List<BookCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val backupAvailable: Boolean = false
)
```

Допустимое расширение состояния другими полями не отменяет требования к единому
источнику истины для одного экрана.

### 3.3. ViewModel

Каждому Activity соответствует отдельная ViewModel:

| Activity | ViewModel |
|---|---|
| `StartActivity` | `StartViewModel` |
| `RegistrationActivity` | `RegistrationViewModel` |
| `ContentActivity` | `ContentViewModel` |
| `SettingsActivity` | `SettingsViewModel` |

ViewModel:

- принимает действия пользователя через публичные методы;
- валидирует ввод и обновляет `StateFlow<UiState>`;
- вызывает репозитории и хранилища;
- не содержит ссылок на `Activity`, `Context`, `View` или `NavController`;
- переживает изменение конфигурации;
- отменяет фоновые операции при очистке через `viewModelScope`.

ViewModel не должна закрывать или запускать Activity. Навигационное следствие
представляется одноразовым событием, например `SharedFlow<StartEvent>`, которое
обрабатывает Activity. Простые переходы без результата допустимо обрабатывать
колбэком из UI.

### 3.4. Domain

Domain-слой содержит модель книги, статус доступности и чистые правила
валидации. Он не импортирует Android UI, Activity, Compose, JSON- или сетевые
библиотеки.

Основные типы:

```kotlin
data class BookCard(
    val id: Int,
    val title: String,
    val author: String,
    val genre: String,
    val year: Int,
    val rating: Double,
    val availability: BookAvailability,
    val coverUrl: String?
)

enum class BookAvailability {
    AVAILABLE,
    ON_LOAN,
    UNAVAILABLE
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}
```

Требования к domain-логике:

- одна функция проверяет одно правило;
- сообщения об ошибках не зависят от Activity;
- проверка регистрации и входа использует одинаковые правила для логина и
  пароля;
- доменная модель не содержит `Bitmap`, `Uri` или Compose-типы;
- отсутствие доступной книги не считается ошибкой загрузки.

### 3.5. Data

Слой данных реализует контракты, определённые для UI/domain:

```kotlin
interface BookRepository {
    suspend fun getBooks(): List<BookCard>
    suspend fun refreshBooks(): List<BookCard>
}

interface BackupRepository {
    suspend fun createBackup(books: List<BookCard>): BackupInfo
    suspend fun restoreBackup(): List<BookCard>
    suspend fun deleteBackup()
    suspend fun getBackupInfo(): BackupInfo?
}

interface UserRepository {
    suspend fun register(profile: UserProfile, password: CharArray): Result<Unit>
    suspend fun authenticate(login: String, password: CharArray): Result<UserProfile>
    suspend fun getCurrentProfile(): UserProfile?
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
}
```

Допускается уточнение сигнатур, если сохраняются границы ответственности и
асинхронность. Реализации не должны протекать в UI: Compose-код не работает с
`File`, `JSONObject`, HTTP-клиентом или `SharedPreferences` напрямую.

Для ЛР 1 используются две реализации `BookRepository`:

- `DemoBookRepository` — возвращает 12 встроенных книг и является активным
  источником;
- будущая `RemoteBookRepository` — контракт для ЛР 3, не требуется в текущей
  сдаче.

### 3.6. Storage

Хранилище делится по назначению:

| Данные | Место хранения | Формат |
|---|---|---|
| профиль пользователя | DataStore/SharedPreferences | примитивные поля |
| соль и хеш пароля | DataStore/SharedPreferences | Base64 |
| настройки темы и шрифта | DataStore/SharedPreferences | примитивные поля |
| каталог в кэше | `filesDir`/`cacheDir` | JSON |
| резервная копия | `filesDir/backup` | `content_backup.json` |

Пароль в открытом виде не хранится ни в памяти после завершения проверки, ни в
файле, ни в логах. Для ЛР 1 разрешён salted SHA-256, но предпочтителен
`PBKDF2WithHmacSHA256`. Соль уникальна для пользователя. Это учебная реализация
локальной проверки, не замена серверной аутентификации.

## 4. Потоки данных

### Вход

```text
поле UI → RegistrationViewModel/StartViewModel → PasswordHasher
→ UserRepository → DataStore → StateFlow → StartActivity
```

### Каталог

```text
DemoBookRepository → ContentViewModel → StateFlow<ContentUiState>
→ LazyColumn → BookCard composable
```

### Резервная копия

```text
Settings UI → SettingsViewModel → BackupRepository
→ content_backup.json → BackupInfo → StateFlow → Settings UI
```

Одноразовые сообщения (`Success`, `Error`, `Navigate`) передаются отдельно от
постоянного состояния. Повторное отображение экрана не должно повторять
уведомление или переход.

## 5. Навигационный контракт

```text
StartActivity ──«Регистрация»──▶ RegistrationActivity
RegistrationActivity ──RESULT_OK(login)──▶ StartActivity
StartActivity ──успешный вход──▶ ContentActivity [finish StartActivity]
ContentActivity ──«Настройки»──▶ SettingsActivity [startActivityForResult]
SettingsActivity ──RESULT_OK(settings changed)──▶ ContentActivity [finish SettingsActivity]
```

Требования:

- `StartActivity` использует `registerForActivityResult` для регистрации;
- успешная регистрация возвращает логин через `Intent` с ключом
  `EXTRA_LOGIN`, а не глобальную переменную;
- после входа активность стартового экрана закрывается;
- настройки возвращают только к каталогу; глубина истории от настроек до
  стартового экрана не должна превышать два экрана;
- повторный клик по навигационной кнопке не создаёт вторую Activity;
- передача профиля и пароля в Intent запрещена; между Activity передаётся только
  идентификатор или логин;
- восстановление после поворота экрана не должно повторно выполнять успешный
  вход, регистрацию или восстановление копии.

Для межэкранного результата используется типизированный контракт:

```kotlin
object RegistrationContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit) =
        Intent(context, RegistrationActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent?.getStringExtra(RegistrationActivity.EXTRA_LOGIN)
            ?.takeIf { resultCode == Activity.RESULT_OK }
}
```

## 6. Жизненный цикл и журналирование

Все четыре Activity наследуют `LoggedActivity : ComponentActivity()`.
Базовый класс переопределяет методы и пишет записи через единый тег
`Lab1Lifecycle`:

```kotlin
open class LoggedActivity : ComponentActivity() {
    protected fun logLifecycle(method: String) {
        Log.d("Lab1Lifecycle", "${this::class.simpleName}: $method")
    }
}
```

Обязательно логируются:

`onCreate`, `onStart`, `onRestart`, `onResume`, `onPause`, `onStop`,
`onDestroy`.

В каждом переопределении сначала вызывается `super`, затем пишется запись.
Формат строки строго `[класс]: [метод]` без дополнительного префикса между
ними. В отчёт включаются фрагменты для частичного перекрытия и полного маршрута
из PDF.

## 7. Обработка ошибок и состояний

Каждая операция UI классифицируется как `Idle`, `Loading`, `Success` или
`Error`. Для экрана каталога дополнительно предусматривается `Empty`.

| Ситуация | Поведение |
|---|---|
| неверный ввод формы | ошибка под соответствующим полем |
| неверные учётные данные | общая ошибка формы, поля не очищаются |
| пустой каталог | пустое состояние с доступными действиями |
| ошибка чтения JSON | сообщение об ошибке, файл не перезаписывается |
| отсутствие копии | кнопки восстановления и удаления недоступны |
| системная ошибка записи | успех не показывается, исходные данные сохраняются |

Исключения не должны завершать приложение. Граница преобразования исключений —
ViewModel или репозиторий; UI получает типизированное или понятное пользователю
сообщение. В Logcat не выводятся пароли, хеши, персональные данные и полное
содержимое файлов.

## 8. Тестируемость

Domain-валидация и преобразования данных должны тестироваться без Android
эмулятора. Репозитории и хранилища передаются в ViewModel через интерфейсы.
Запрещено создавать синглтоны хранилища непосредственно внутри ViewModel, если
это мешает подмене реализации в тесте.

Минимальный набор проверок:

- границы логина, пароля, имени и возраста;
- успешная и неуспешная регистрация;
- успешная и неуспешная аутентификация;
- создание, чтение и удаление резервной копии;
- обработка некорректного JSON;
- состав и количество карточек каталога;
- корректность маршрутов Activity и отсутствие дублирования;
- наличие строк жизненного цикла всех Activity.

## 9. Структура проекта

Целевая структура для пакета `com.example.a1lab`:

```text
com.example.a1lab
├── StartActivity.kt
├── RegistrationActivity.kt
├── ContentActivity.kt
├── SettingsActivity.kt
├── logging/
│   └── LoggedActivity.kt
├── domain/
│   ├── BookCard.kt
│   ├── BookAvailability.kt
│   ├── UserProfile.kt
│   └── Validation.kt
├── data/
│   ├── BookRepository.kt
│   ├── DemoBookRepository.kt
│   ├── BackupRepository.kt
│   ├── JsonBackupRepository.kt
│   └── UserRepository.kt
├── storage/
│   ├── PreferenceUserStorage.kt
│   └── SettingsStorage.kt
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

Разделение по пакетам обязательно по смыслу, точные имена файлов можно
уточнять. Не допускается размещать все Activity, ViewModel, модели и работу с
файлами в одном файле.

## 10. Совместимость с последующей сетевой работой

Сетевой этап должен подключаться через реализацию `BookRepository`, а не через
изменения экранов. Сетевые DTO не передаются в Compose. Маппинг DTO → `BookCard`
находится в data-слое.

Для будущего API предусматриваются:

- загрузка данных порциями;
- явные состояния загрузки и ошибки;
- сохранение успешно полученного каталога в локальный кэш;
- выбор между кэшем и резервной копией при отсутствии сети;
- отмена запросов при уходе с экрана.

Эти возможности не требуется реализовывать в ЛР 1, но архитектура не должна
препятствовать их добавлению.

## 11. Архитектурные критерии приёмки

- [ ] В проекте есть четыре Activity, назначенные в манифесте.
- [ ] `StartActivity` объявлена единственной LAUNCHER-активностью.
- [ ] Каждый экран получает данные из ViewModel или stateless-состояния.
- [ ] UI не работает с файлами, паролями и источниками данных напрямую.
- [ ] Модель `BookCard` не зависит от Android UI и формата API.
- [ ] Сетевой источник можно заменить без изменения экранов каталога.
- [ ] Профиль, настройки и каталог имеют явные места хранения.
- [ ] Резервная копия — отдельный JSON-файл с версией формата.
- [ ] Пароли не сохраняются и не логируются в открытом виде.
- [ ] Фоновые операции выполняются через Coroutines и `viewModelScope`.
- [ ] Четыре Activity наследуют общий `LoggedActivity`.
- [ ] Логи содержат только имя класса и метода в требуемом формате.
- [ ] Навигация с использованием результата реализована через Activity Result
      API; неактуальные экраны закрываются.
- [ ] Domain-логика имеет unit-тесты, не требующие Android-эмулятора.
