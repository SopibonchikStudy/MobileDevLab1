# Защита лабораторной работы: классы и блоки кода

> Материал составлен по текущему состоянию проекта **Punchline** в каталоге
> `app/src/main/java/com/example/a1lab`. Он объясняет назначение каждого
> объявленного класса, его основные методы и связи с остальными частями
> приложения. Для защиты достаточно сначала рассказать общую архитектуру, затем
> пройти по четырём Activity, состоянию и хранению данных, а ключевые блоки кода
> показывать по исходникам.

## 1. Краткая формулировка проекта

**Punchline** — Android-приложение на Kotlin и Jetpack Compose. Пользователь
локально регистрируется, входит в приложение и получает коллекцию из 12–20
англоязычных шуток JokeAPI. Загруженная коллекция сохраняется в JSON-кэш и
может быть записана в резервную копию. Профиль и настройки хранятся в
`SharedPreferences`, а пароль — только в виде соли и хеша.

Главная идея архитектуры: **Activity управляет экраном и навигацией, ViewModel —
сценарием и состоянием, репозиторий — сетью и хранением, Compose — отображением**.

Технологии:

| Технология | Где используется |
|---|---|
| Kotlin | весь прикладной код |
| Jetpack Compose и Material 3 | все четыре экрана, карточки, поля, диалоги |
| Четыре Activity | старт, регистрация, шутки, настройки |
| ViewModel и `StateFlow` | состояние экранов |
| `Channel` | одноразовые сообщения и навигационные события |
| Coroutines | сетевые и файловые операции |
| `HttpURLConnection` и `org.json` | JokeAPI и JSON-кодек |
| `SharedPreferences` | пользователь и настройки |
| JSON-файлы | кэш и резервная копия |

Ключевое ограничение проекта: авторизация локальная, серверного аккаунта нет.
Это учебная реализация для одного устройства.

## 2. Архитектура одним взглядом

```text
AndroidManifest
      │
      ▼
PunchlineApplication ──создаёт──► AppContainer
                                      │
              ┌───────────────────────┼───────────────────────┐
              ▼                       ▼                       ▼
       UserRepository          JokeRepository        BackupRepository
       SettingsRepository
              │                       │                       │
              ▼                       ▼                       ▼
       SharedPreferences      JokeAPI/JSON-кэш       JSON-резервная копия
              │
              ▲
       Activity ──создаёт──► ViewModel
          │                      │
          │ collect StateFlow    │ вызывает suspend-методы
          ▼                      ▼
       Compose UI ◄──── UiState/события
```

Однонаправленный поток данных:

```text
Действие пользователя
  → колбэк Compose
  → метод ViewModel
  → domain-валидация и data-репозиторий
  → MutableStateFlow / Channel
  → Activity собирает StateFlow и события
  → Compose перерисовывает экран
```

Почему так:

- Activity не выполняет HTTP-запросы и не разбирает JSON;
- ViewModel не хранит `Activity`, `Context` или composable;
- domain-классы не зависят от Android UI;
- Compose-экраны получают готовое состояние и колбэки;
- реализации репозиториев можно заменить без переписывания экранов;
- сеть и файлы выполняются в `Dispatchers.IO`.

## 3. Полная карта классов

В таблице перечислены все объявленные типы приложения, включая `object`,
`interface`, `enum class`, data-классы и вложенные классы. Composable-функции
не являются классами и разобраны отдельно в разделе 9.

| Тип | Файл | Главная ответственность |
|---|---|---|
| `PunchlineApplication` | `PunchlineApplication.kt` | создание общего `AppContainer` |
| `StartActivity` | `StartActivity.kt` | стартовый экран и вход |
| `RegistrationActivity` | `RegistrationActivity.kt` | регистрация и возврат логина |
| `ContentActivity` | `ContentActivity.kt` | лента шуток и переход в настройки |
| `SettingsActivity` | `SettingsActivity.kt` | профиль, оформление и копия |
| `StartViewModel` | `viewmodel/StartViewModel.kt` | вход и загрузка текущего профиля |
| `StartUiState` | `viewmodel/StartViewModel.kt` | состояние стартового экрана |
| `StartEvent` | `viewmodel/StartViewModel.kt` | одноразовые события входа |
| `RegistrationViewModel` | `viewmodel/RegistrationViewModel.kt` | проверка и создание аккаунта |
| `RegistrationUiState` | `viewmodel/RegistrationViewModel.kt` | значения и ошибки формы |
| `ContentViewModel` | `viewmodel/ContentViewModel.kt` | загрузка, обновление и восстановление |
| `ContentUiState` | `viewmodel/ContentViewModel.kt` | состояние ленты |
| `SettingsViewModel` | `viewmodel/SettingsViewModel.kt` | профиль, настройки и операции с файлами |
| `SettingsUiState` | `viewmodel/SettingsViewModel.kt` | состояние экрана настроек |
| `SettingsEvent` | `viewmodel/SettingsViewModel.kt` | сообщения и изменение профиля |
| `UiMessage` | `viewmodel/UiMessage.kt` | независимые от языка ключи сообщений |
| `UserProfile` | `domain/UserProfile.kt` | доменная модель пользователя |
| `JokeCard` | `domain/JokeCard.kt` | доменная модель шутки |
| `JokeCategory` | `domain/JokeCard.kt` | категории шуток |
| `JokeType` | `domain/JokeCard.kt` | формат шутки |
| `ThemeMode` | `domain/AppSettings.kt` | системная, светлая или тёмная тема |
| `AppLanguage` | `domain/AppSettings.kt` | язык интерфейса |
| `AppSettings` | `domain/AppSettings.kt` | объединённые настройки оформления |
| `ValidationResult` | `domain/Validation.kt` | результат проверки поля |
| `ValidationError` | `domain/Validation.kt` | ключ ошибки без текста языка UI |
| `Validation` | `domain/Validation.kt` | правила проверки ввода |
| `UserRepository` | `data/UserRepository.kt` | контракт работы с пользователем |
| `SettingsRepository` | `data/SettingsRepository.kt` | контракт настроек |
| `JokeRepository` | `data/JokeRepository.kt` | контракт загрузки и кэша |
| `JokeFeedPolicy` | `data/JokeRepository.kt` | ограничения и дедупликация ленты |
| `BackupInfo` | `data/BackupRepository.kt` | метаданные копии |
| `BackupRepository` | `data/BackupRepository.kt` | контракт резервной копии |
| `AppContainer` | `data/AppContainer.kt` | ручная сборка зависимостей |
| `RemoteJokeRepository` | `data/RemoteJokeRepository.kt` | JokeAPI, кэш и атомарная запись |
| `JokeJsonCodec` | `data/JokeJsonCodec.kt` | преобразование JSON ↔ `JokeCard` |
| `JsonBackupRepository` | `data/JsonBackupRepository.kt` | JSON-копия во внутреннем хранилище |
| `PreferenceUserRepository` | `storage/PreferenceUserRepository.kt` | профиль и учётные данные |
| `PasswordHasher` | `storage/PasswordHasher.kt` | соль и учебный хеш пароля |
| `PreferenceSettingsRepository` | `storage/PreferenceSettingsRepository.kt` | тема, шрифт и язык |
| `RegistrationContract` | `navigation/ActivityContracts.kt` | типизированный запуск регистрации |
| `SettingsContract` | `navigation/ActivityContracts.kt` | типизированный запуск настроек |
| `SettingsResult` | `navigation/ActivityContracts.kt` | результат возврата из настроек |
| `LoggedActivity` | `logging/LoggedActivity.kt` | общий журнал жизненного цикла |

Вложенные типы также разобраны ниже:

- `StartEvent.Authenticated`, `StartEvent.Message`;
- `SettingsEvent.Message`, `SettingsEvent.ProfileUpdated`;
- `ValidationResult.Valid`, `ValidationResult.Invalid`;
- `PasswordHasher.StoredPassword`;
- `JsonBackupRepository.BackupContent`;
- `SettingsActivity.PendingAction`;
- `SettingsScreen.AvatarOption`.

## 4. Корневые Activity и точка входа

### 4.1. `PunchlineApplication`

Файл: `app/src/main/java/com/example/a1lab/PunchlineApplication.kt`.

Наследуется от `Application` и создаёт граф зависимостей при старте процесса.

```kotlin
override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
}
```

`lateinit var container` доступен Activity через приведение
`application as PunchlineApplication`. Это простой ручной dependency injection.
Контейнер создаётся один раз, а не отдельно на каждом экране.

Что сказать:

> «Application не показывает интерфейс. Он служит точкой создания общих
> репозиториев, которые затем передаются ViewModel через фабрики».

### 4.2. `StartActivity`

Файл: `StartActivity.kt`.

Роль: стартовый экран, различающий первый и повторный вход.

Поля и зависимости:

- `container` — получает репозитории из `PunchlineApplication`;
- `viewModel` — создаётся через `viewModels { viewModelFactory { ... } }`;
- `registrationLauncher` — Activity Result API для регистрации;
- `navigating` — защита от повторного открытия `ContentActivity`;
- `pendingMessage` — сообщение, ожидающее показа в `Snackbar`.

Основной метод `onCreate`:

1. вызывает `super.onCreate`;
2. включает edge-to-edge;
3. подписывается на `StartViewModel.state` и настройки;
4. передаёт `StartScreen` состояние и колбэки;
5. собирает `StartEvent` из `Channel`;
6. при `Authenticated` открывает `ContentActivity` и закрывает текущую Activity.

```kotlin
startActivity(Intent(this, ContentActivity::class.java).putExtra(...))
finish()
```

`finish()` нужен, чтобы экран входа не оставался под лентой. `navigating`
предотвращает обработку повторного события.

`registerForActivityResult(RegistrationContract)` получает назад только логин.
Если результат `null`, то пользователь отменил регистрацию.

Нижняя функция `displayContext(context, localeTag)` создаёт контекст с нужной
локалью для строкового сообщения. Она не меняет глобальную локаль устройства.

Что защищать:

- первый вход определяется через загруженный профиль, а не через отдельный
  глобальный флаг;
- навигация выполняется Activity, а не ViewModel;
- пароль не передаётся в Intent.

### 4.3. `RegistrationActivity`

Файл: `RegistrationActivity.kt`.

Роль: показать форму регистрации и вернуть результат на стартовый экран.

Зависимость только одна — `UserRepository`; ViewModel создаёт
`RegistrationViewModel`.

`OnBackPressedCallback` завершает Activity. Это делает системную кнопку «Назад»
одинаково понятной кнопке возврата на форме.

Колбэки экрана:

- `updateName`, `updateLogin`, `updateAge`;
- `updatePassword`, `updatePasswordConfirmation`;
- `register`.

После успешной регистрации ViewModel отправляет логин в `Channel`. Activity
формирует результат:

```kotlin
setResult(RESULT_OK, Intent().putExtra(EXTRA_LOGIN, login))
finish()
```

Существенный момент: обратно передаётся **только логин**, без пароля, соли или
хеша.

### 4.4. `ContentActivity`

Файл: `ContentActivity.kt`.

Роль: показать ленту шуток и открыть настройки.

Поля:

- `viewModel` типа `ContentViewModel`;
- `settingsLauncher` для `SettingsContract`;
- `settingsOpen` — защита от повторного запуска настроек;
- `settingsLogin` — логин, полученный при входе;
- `pendingMessage` — одноразовое сообщение для Snackbar.

`settingsLauncher` после возврата:

1. сбрасывает `settingsOpen`;
2. вызывает `viewModel.load(showMessage = true)`;
3. при `profileChanged` дополнительно обновляет отображаемый профиль.

Почему так: настройки могут изменить кэш или профиль. Повторное чтение через
ViewModel синхронизирует экран с актуальным состоянием.

`BackHandler` перехватывает системный возврат. Первое нажатие открывает
`AlertDialog`, подтверждение вызывает `finish()`. Это защищает от случайного
выхода и не затрагивает настройки, где возврат ведёт обратно к ленте.

Компонент `ContentScreen` получает:

- `state`;
- `onOpenSettings`;
- `onRefresh`;
- `onRestoreBackup`;
- `snackbarHost`.

### 4.5. `SettingsActivity`

Файл: `SettingsActivity.kt`.

Роль: редактирование профиля и оформления, работа с кэшем и копией.

ViewModel получает четыре зависимости:

- `UserRepository`;
- `SettingsRepository`;
- `BackupRepository`;
- `JokeRepository`.

Почему четыре: экран не только сохраняет профиль, но и создаёт копию текущей
коллекции, восстанавливает её в кэш, удаляет копию и очищает кэш.

Поле `profileChanged` сохраняется в `Bundle` в
`onSaveInstanceState`. Оно нужно, чтобы после пересоздания Activity корректно
сообщить `ContentActivity`, что профиль изменён.

Диалоги создаются по `PendingAction`. Перед операцией пользователь выбирает
действие, затем подтверждает его в `AlertDialog`:

- `CREATE` — создать копию;
- `RESTORE` — восстановить копию;
- `DELETE` — удалить копию;
- `CLEAR` — очистить кэш.

`returnToContent()` формирует `SettingsResult` и завершает Activity. Один метод
используется и toolbar-кнопкой, и системным «Назад».

Вложенный `PendingAction` хранит для каждого действия ресурсы заголовка и текста
диалога. Это убирает длинную цепочку `if` в composable.

## 5. ViewModel и UI-состояния

Общий шаблон всех ViewModel:

```text
UI-событие → метод ViewModel → репозиторий
           ← StateFlow      ← новое UiState
```

`MutableStateFlow` закрыт внутри класса, наружу отдаётся read-only
`StateFlow`. Однократные следствия передаются через буферизованный `Channel`.

### 5.1. `StartViewModel`

Файл: `viewmodel/StartViewModel.kt`.

`init` вызывает `loadExistingUser()`. Если профиль уже есть, ViewModel
устанавливает `isReturning = true`, имя, аватар и предзаполняет логин.

Методы:

| Метод | Что делает |
|---|---|
| `updateLogin` | меняет логин и сбрасывает старую ошибку |
| `updatePassword` | меняет пароль и сбрасывает старую ошибку |
| `onRegistrationReturned` | принимает логин после регистрации и обновляет экран |
| `authenticate` | валидирует данные и вызывает `UserRepository.authenticate` |
| `loadExistingUser` | читает профиль для различения первого и повторного входа |

В `authenticate` сначала выполняется валидация. Только после неё запускается
корутина в `viewModelScope`. При успехе отправляется
`StartEvent.Authenticated(profile.login)`. При ошибке выставляется
`UiMessage.INVALID_CREDENTIALS`.

`passwordChars.fill('\u0000')` выполняется и при ошибке, и после завершения
асинхронной проверки. Так очищаются локальные байтовые/символьные копии
пароля.

`StartUiState` — единый снимок экрана: поля, ошибки, `isSubmitting`, профиль.

`StartEvent`:

- `Authenticated(login)` — навигация;
- `Message(message)` — Snackbar.

События не хранятся в `UiState`, поэтому поворот экрана не обязан повторять
переход или сообщение.

### 5.2. `RegistrationViewModel`

Файл: `viewmodel/RegistrationViewModel.kt`.

`RegistrationUiState` содержит значения всех полей и отдельные ошибки. Ошибки
имеют тип `ValidationError`, а не готовый русский или английский текст. Поэтому
domain-слой не зависит от ресурсов Android.

`update...`-методы очищают ошибку только соответствующего поля. Общая ошибка
`formError` сбрасывается при новом вводе.

`register()`:

1. блокирует повторную отправку через `isSubmitting`;
2. создаёт `CharArray` для пароля и подтверждения;
3. проверяет имя, логин, возраст, пароль и подтверждение;
4. при ошибке записывает все найденные ключи в состояние;
5. очищает массивы и не вызывает репозиторий;
6. при успешной проверке формирует `UserProfile`;
7. вызывает `userRepository.register(profile, password)`;
8. при успехе очищает поля пароля и отправляет логин событием;
9. при ошибке выставляет `ACCOUNT_CREATE_FAILED`;
10. в любом случае затирает локальные массивы пароля.

Вложенный `update {}` — небольшая функция для атомарной замены состояния
через `copy`.

### 5.3. `ContentViewModel`

Файл: `viewmodel/ContentViewModel.kt`.

`ContentUiState` содержит имя, аватар, список `JokeCard`, флаг загрузки, ключ
ошибки и доступность копии.

Методы:

| Метод | Сценарий |
|---|---|
| `load()` | читает профиль, шутки и наличие копии |
| `refresh()` | делает явный сетевой запрос |
| `reloadProfile()` | обновляет имя и аватар после настроек |
| `restoreBackup()` | читает копию и заменяет ею кэш |

`load()` важен для офлайн-сценария. Если загрузка не удалась, но текущие шутки
уже есть, список остаётся на экране, а ошибка отправляется через Snackbar.
Если шуток нет, выставляется `errorMessage` и UI показывает отдельное состояние
ошибки.

`refresh()` сначала выставляет `isLoading`, затем вызывает
`jokeRepository.refreshJokes()`. Успешный результат заменяет список. Ошибка
обновления не удаляет уже загруженные карточки.

`restoreBackup()` выполняет две разные операции:

```kotlin
backupRepository.restoreBackup().also { jokeRepository.replaceJokes(it) }
```

Сначала читается резервная копия, затем она записывается в текущий кэш. В UI
сразу возвращается восстановленный список.

### 5.4. `SettingsViewModel`

Файл: `viewmodel/SettingsViewModel.kt`.

`SettingsUiState` хранит:

- исходный `UserProfile`;
- редактируемые имя и возраст строками;
- аватар;
- ошибки имени и возраста;
- `AppSettings`;
- `BackupInfo?`;
- `isLoading`;
- `hasChanges`.

Методы профиля:

- `updateName`, `updateAge`, `updateAvatar` меняют draft-состояние;
- `saveProfile()` проверяет его и вызывает `UserRepository.updateProfile`;
- после успеха отправляет `ProfileUpdated` и `PROFILE_SAVED`.

Методы оформления:

- `updateTheme`;
- `updateFontScale`;
- `updateLanguage`.

Они сразу сохраняют настройки через `SettingsRepository` и обновляют локальный
`SettingsUiState`. Поэтому тема, язык и масштаб применяются без отдельной кнопки
«Сохранить».

Методы данных:

- `createBackup()` берёт текущие шутки через `JokeRepository` и вызывает
  `BackupRepository.createBackup`;
- `restoreBackup()` восстанавливает копию и записывает её в кэш;
- `deleteBackup()` удаляет файл и обнуляет `backupInfo`;
- `clearCatalog()` очищает кэш, но не удаляет саму копию.

Каждая операция защищена от повторного запуска проверкой `isLoading` и
заключена в `runCatching`. Ошибка превращается в `UiMessage`, а не в падение
приложения.

`load()` читает профиль, `BackupInfo` и текущие настройки.

`SettingsEvent`:

- `Message` — показать Snackbar;
- `ProfileUpdated` — сообщить Activity, что профиль изменён.

### 5.5. `UiMessage`

Файл: `viewmodel/UiMessage.kt`.

Перечисление содержит ключи пользовательских сообщений: успех регистрации,
неверные данные, ошибка загрузки, результаты работы с копией и другие.

Текст не хранится в ViewModel, потому что приложение поддерживает русский,
английский и системный язык. `UiMessage` превращается в строковый ресурс в UI.

## 6. Domain-слой

Domain-слой не зависит от Android, Compose, HTTP и `JSONObject`.

### 6.1. `UserProfile`

```kotlin
data class UserProfile(
    val login: String,
    val displayName: String,
    val age: Int? = null,
    val avatar: String = "laugh"
)
```

Неизменяемая модель профиля. `age` необязателен; при сохранении отсутствующего
возраста в `SharedPreferences` записывается `0`, а при чтении `0` снова
превращается в `null`.

`avatar` — идентификатор варианта оформления, а не путь к изображению.

### 6.2. `JokeCard`

Главная модель контента:

- `id` — уникальный ключ и основа дедупликации;
- `category` — категория JokeAPI;
- `type` — `SINGLE`, `TWOPART` или `UNKNOWN`;
- `setup` — текст или первая часть шутки;
- `punchline` — необязательная развязка;
- `safe` — признак safe-mode;
- `language` — язык API;
- `wordCount` — вычисляемое число слов.

`JokeCategory` и `JokeType` содержат `UNKNOWN`, чтобы новая категория API не
вызывала исключение.

### 6.3. `AppSettings`, `ThemeMode`, `AppLanguage`

`AppSettings` объединяет тему, масштаб шрифта и язык.

`ThemeMode`:

- `SYSTEM` — следовать теме ОС;
- `LIGHT` — принудительно светлая;
- `DARK` — принудительно тёмная.

`AppLanguage`:

- `SYSTEM`;
- `RUSSIAN`;
- `ENGLISH`.

`localeTag()` возвращает `null` для системного языка, `"ru"` или `"en"`. UI
использует тег для создания локализованного контекста.

### 6.4. `Validation`, `ValidationResult`, `ValidationError`

`ValidationResult` — sealed-интерфейс:

- `Valid` — успех;
- `Invalid(error)` — ошибка с ключом.

`ValidationError` содержит ключи `NAME`, `LOGIN`, `PASSWORD`,
`PASSWORD_CONFIRMATION`, `AGE`.

`Validation` — `object`, то есть единственный экземпляр с функциями:

| Метод | Правило |
|---|---|
| `validateName` | после `trim()` от 2 до 40 символов |
| `validateLogin` | от 3 до 64; при `@` проверяется regex e-mail |
| `validatePassword` | `CharArray` длиной 6–64 |
| `validatePasswordConfirmation` | массивы равны через `contentEquals` |
| `validateAge` | целое от 6 до 120 |

Почему пароль принимается как `CharArray`: его можно затереть после проверки.
Строка `String` в Kotlin неизменяема.

## 7. Data-слой

### 7.1. Интерфейсы репозиториев

`UserRepository` определяет регистрацию, вход, чтение и обновление профиля.
`register` и `authenticate` принимают пароль как `CharArray`; `authenticate`
возвращает `Result<UserProfile>`.

`SettingsRepository` публикует `StateFlow<AppSettings>` и методы изменения темы,
шрифта и языка.

`JokeRepository` отделяет экран от источника данных:

```kotlin
getJokes()
refreshJokes()
replaceJokes(jokes)
clearCachedJokes()
```

`BackupRepository` управляет копией: создать, прочитать, удалить и получить
метаданные.

Эти интерфейсы позволяют заменить реальные данные тестовыми реализациями и
объясняют зависимости в конструкторах ViewModel.

### 7.2. `JokeFeedPolicy`

Внутренний `object` рядом с `JokeRepository`.

Константы:

- минимум 12 шуток;
- максимум 20;
- размер пакета 10;
- максимум три пакета.

`merge(current, incoming)` объединяет списки, оставляет первое вхождение каждого
`id` и ограничивает результат 20 элементами. Сохранение первого вхождения даёт
стабильный порядок.

### 7.3. `AppContainer`

Ручной DI-контейнер. Создаёт:

- `PreferenceUserRepository`;
- `PreferenceSettingsRepository`;
- `RemoteJokeRepository`;
- `JsonBackupRepository`.

Свойства имеют тип интерфейсов. Activity не знает конкретный класс реализации.
Для учебного проекта это проще Hilt/Koin и делает зависимости явными.

### 7.4. `RemoteJokeRepository`

Отвечает за API, кэш и сетевые ограничения.

`getJokes()`:

- если флаг инициализации не выставлен, а кэш уже есть, читает кэш;
- если инициализации и кэша нет, вызывает `refreshJokes()`;
- если инициализация есть, но файла нет, возвращает пустой список;
- иначе читает `jokes_cache.json`.

`refreshJokes()` выполняет до трёх сетевых пакетов, пока не собрано 12
уникальных шуток:

```text
fetchBatch(10)
  → decodeApiResponse
  → merge + distinctBy(id)
  → повтор при недостатке
  → проверка минимум 12
  → атомарная запись кэша
```

Только после успешной проверки записывается новый кэш. Поэтому ошибка
обновления не уничтожает старые данные.

`fetchBatch()`:

- формирует URL с `type=twopart`, `amount=10`, `safe-mode`, `lang=en`;
- задаёт `GET`, `Accept`, `User-Agent`;
- устанавливает таймауты 10 и 15 секунд;
- проверяет HTTP-статус 2xx;
- читает UTF-8;
- декодирует JSON;
- отключает соединение в `finally`.

`clearCachedJokes()` удаляет файл и выставляет флаг инициализации. Поэтому после
очистки лента остаётся пустой до обновления или восстановления копии, а не
загружается автоматически при следующем открытии.

`replaceJokes()` нужен после восстановления копии: сохраняет восстановленный
список в основной кэш, используя тот же формат.

`writeAtomically()`:

1. пишет данные во временный файл `.tmp`;
2. при наличии старого файла переименовывает его в `.previous`;
3. переименовывает `.tmp` в целевой файл;
4. при неудаче пытается вернуть `.previous`;
5. удаляет предыдущую версию после успеха.

Это уменьшает вероятность увидеть частично записанный JSON.

### 7.5. `JokeJsonCodec`

Внутренний `object` для преобразования JSON.

`encode(jokes)` создаёт `JSONArray` и записывает все поля `JokeCard`.
Отсутствующий `punchline` записывается как `JSONObject.NULL`.

`decode(array)` читает кэш. Неизвестные enum-значения заменяются на `UNKNOWN`.
`require` отбрасывает некорректную запись без валидного `id` или текста.

`decodeApiResponse(json)`:

- проверяет `error: true`;
- поддерживает как пакетный массив `jokes`, так и одиночный объект;
- для `twopart` берёт `setup` и `delivery`;
- для `single` берёт `joke`, а `punchline` оставляет `null`;
- вычисляет `wordCount`.

Этот класс — граница между сырым JSON и domain-моделью.

### 7.6. `JsonBackupRepository`

Хранит файл `filesDir/backup/jokes_backup.json`, формат версии 2.

`createBackup(jokes)`:

1. не разрешает пустую коллекцию;
2. создаёт папку `backup`;
3. формирует дату `createdAt`;
4. сериализует `version`, `createdAt`, `jokes`;
5. пишет временный файл;
6. повторно читает его для проверки;
7. атомарно заменяет старую копию;
8. возвращает `BackupInfo`.

`readBackupFile()` проверяет версию, непустой список, уникальность `id` и
корректность каждой шутки.

`getBackupInfo()` возвращает `null`, если файла нет или он не читается. Поэтому
кнопки восстановления и удаления в UI неактивны.

Вложенный `BackupContent` — приватная data-модель для пары «дата + шутки». Она
не нужна UI и остаётся деталью реализации.

`BackupInfo` публичен, потому что ViewModel и настройки показывают дату и число
шуток.

## 8. Storage, навигация и журналирование

### 8.1. `PreferenceUserRepository`

Файл `SharedPreferences` — `punchline_user`, режим `MODE_PRIVATE`.

Ключи:

- логин;
- отображаемое имя;
- возраст;
- аватар;
- соль;
- хеш пароля.

`register()`:

- проверяет непустые имя и логин;
- не разрешает второй локальный аккаунт;
- создаёт соль и хеш;
- записывает профиль и учётные данные одной транзакцией;
- возвращает `Result<Unit>`.

Используется `commit()` внутри `Dispatchers.IO`, чтобы результат записи был
известен сразу. Для обычных настроек используется более лёгкий `apply()`.

`authenticate()`:

- читает логин, соль и хеш;
- сравнивает логин без учёта регистра;
- проверяет пароль через `PasswordHasher.verify`;
- возвращает профиль или `Result.failure`.

`readProfile()` — общий метод чтения профиля. Если возраст равен 0, он
превращается в `null`.

### 8.2. `PasswordHasher`

Поля конструктора:

- число итераций `20_000`;
- `SecureRandom` для соли.

`StoredPassword` — вложенная data-модель с `salt` и `hash` в Base64.

`create(password)`:

1. создаёт 16 случайных байт соли;
2. вычисляет учебный итеративный HMAC-SHA256;
3. кодирует соль и результат через Base64 без переносов строк.

`verify(password, salt, expectedHash)` повторяет вычисление с сохранённой солью
и сравнивает результат через `constantTimeEquals`.

`derive()` использует пароль как HMAC-ключ, добавляет соль и счётчик, а затем
многократно перемешивает предыдущий блок с XOR. В конце байтовый массив пароля
затирается нулями в `finally`.

Корректная формулировка для защиты:

> «Это учебная salted-хеш-схема на HMAC-SHA256 с 20 000 итераций и сравнением
> за постоянное время. В production я бы использовал проверенную библиотеку
> PBKDF2, scrypt или Argon2 с рекомендованными параметрами».

Не следует говорить, что это полноценный стандарт PBKDF2: алгоритм реализован
самостоятельно.

### 8.3. `PreferenceSettingsRepository`

Файл настроек — `punchline_settings`.

При создании читает сохранённые значения в `MutableStateFlow`, наружу публикует
read-only `StateFlow`.

Методы:

- `updateTheme` сохраняет имя enum и обновляет поток;
- `updateFontScale` ограничивает масштаб диапазоном `0.85..1.3` и сохраняет;
- `updateLanguage` сохраняет язык.

`readSettings()` безопасно читает enum через `runCatching`; повреждённое
значение заменяется системным, а не вызывает падение.

### 8.4. `RegistrationContract`

`ActivityResultContract<Unit, String?>`.

`createIntent` явно создаёт Intent на `RegistrationActivity`. `parseResult`
возвращает логин только при `RESULT_OK`; иначе возвращает `null`.

Это типизированная замена `startActivityForResult`.

### 8.5. `SettingsContract` и `SettingsResult`

Входной тип контракта — логин, выходной — `SettingsResult?`.

`SettingsResult` содержит:

- `login`;
- `profileChanged`.

`parseResult` возвращает объект только при успешном результате и непустом
Intent. Контракт явно фиксирует, какие данные разрешено передавать между
Activity.

### 8.6. `LoggedActivity`

Общий базовый класс для четырёх Activity. Наследуется от
`ComponentActivity`.

Переопределены `onCreate`, `onStart`, `onRestart`, `onResume`, `onPause`,
`onStop`, `onDestroy`.

Сначала вызывается `super`, затем:

```kotlin
Log.d("Lab1Lifecycle", "${this::class.simpleName}: $method")
```

Пример:

```text
ContentActivity: onCreate
ContentActivity: onStart
ContentActivity: onResume
```

Тег общий, имя Activity берётся из класса. Секреты и содержимое шуток не
логируются.

## 9. Compose-экраны и компоненты

Все экраны — stateless-функции: получают `UiState` и колбэки. Они не создают
ViewModel и не обращаются к файлам или сети напрямую.

### 9.1. `StartScreen`

Получает `StartUiState` и действия входа/регистрации.

Структура:

- `BrandedBackground`;
- прокручиваемый `Column`;
- `BrandHeader` с логотипом и слоганом;
- описание коллекции;
- `Card` с формой;
- поле логина;
- поле пароля;
- ошибка формы;
- основная кнопка входа или продолжения;
- кнопка создания аккаунта;
- примечание о локальном хранении.

Ветвление `state.isReturning` меняет заголовок и показывает аватар с
приветствием. `state.isSubmitting` блокирует поля и показывает progress
indicator на кнопке.

### 9.2. `RegistrationScreen`

Показывает форму из пяти полей:

- имя;
- логин/e-mail;
- возраст;
- пароль;
- подтверждение.

Каждому полю передаются значение, `isError` и текст ошибки. `verticalScroll`
защищает форму от обрезания на небольшом экране или при открытой клавиатуре.
`enabled = !state.isSubmitting` исключает изменение данных во время записи.

### 9.3. `ContentScreen`

Получает `ContentUiState` и четыре UI-действия.

`Scaffold` создаёт `TopAppBar`, область Snackbar и передаёт `PaddingValues` в
содержимое. Затем выбирается одно из четырёх состояний:

| Условие | Что показывается |
|---|---|
| `isLoading` и нет шуток | индикатор загрузки |
| `errorMessage` и нет шуток | `ErrorState` |
| список пуст | `EmptyState` |
| есть шутки | `LazyColumn` |

`HeaderPanel` показывает приветствие, аватар, число шуток и кнопку обновления.

`JokeCardItem` показывает текст, необязательный панчлайн, категорию, тип,
количество слов и графический бейдж. Панчлайн отображается только если он не
`null` и не пустой.

`key = { it.id }` в `LazyColumn` помогает Compose сохранять идентичность
элементов при изменении списка. `JokeBadge` использует
`contentDescription`, поэтому значок доступен screen reader.

`categoryText` и `typeText` переводят enum в строковые ресурсы. Ветка
`UNKNOWN` делает разбор новых данных API безопасным.

### 9.4. `EmptyState` и `ErrorState`

`EmptyState` объясняет отсутствие данных и даёт две кнопки:

- восстановить копию, если `backupAvailable`;
- открыть настройки.

`ErrorState` показывает текст ошибки, повторную загрузку и переход в настройки.

### 9.5. `SettingsScreen`

Содержит три карточки:

1. **Профиль** — логин, имя, возраст, аватар и сохранение;
2. **Оформление** — тема, масштаб шрифта, язык;
3. **Данные** — состояние копии и кнопки создания, восстановления, удаления и
   очистки кэша.

`AvatarOption` — приватный enum с идентификатором и строковым ресурсом. Его
`entries` используются для построения `FilterChip`.

`SettingsCard` устраняет повторение параметров `Card` для трёх секций.

`formatDate` разбирает ISO-дату и выводит её по локали интерфейса. Если формат
повреждён, возвращает исходную строку без падения.

Кнопки восстановления и удаления получают
`enabled = backup != null && !state.isLoading`; при отсутствии файла они
недоступны.

### 9.6. `PunchlineTextField` и `FieldError`

Единый компонент поля ввода:

- принимает значение и колбэк;
- показывает label;
- умеет режим пароля и цифровой клавиатуры;
- показывает `supportingText` с ошибкой;
- может быть `disabled` во время операции.

`PasswordVisualTransformation` скрывает символы. `FieldError` используется для
общей ошибки формы.

### 9.7. `BrandedBackground`, `PunchlineLogo`, `BrandHeader`, `SectionTitle`, `InitialsBadge`

Общие элементы визуальной системы:

- `BrandedBackground` задаёт цвет фона;
- `PunchlineLogo` рисует квадратный знак «P» средствами Compose;
- `BrandHeader` размещает логотип, название и слоган;
- `SectionTitle` задаёт единый заголовок секции;
- `InitialsBadge` получает инициалы из первых двух слов или выбранный символ
  аватара.

Так интерфейс остаётся согласованным между экранами и не требует изображений.

### 9.8. `ValidationError.text()` и `UiMessage.text()`

Файл `ui/components/UiText.kt` содержит mapping из domain/viewmodel-ключей в
`R.string`. Это место, где ключ становится локализованным текстом.

## 10. Тема приложения

### 10.1. `Color.kt`

Содержит именованные цвета светлой и тёмной схем: фон, поверхность, основной
текст, вторичный текст, границы, зелёный акцент и ошибки.

Это обычные `val`, а не классы. Они используются в `Theme.kt`.

### 10.2. `Type.kt`

Содержит базовую `Typography` Material 3 и задаёт размеры для нескольких
ключевых стилей. Остальные стили берутся из стандартной темы Material 3.

### 10.3. `Theme.kt` и `PunchlineTheme`

Создаёт `LightColorScheme` и `DarkColorScheme`. `PunchlineTheme` выбирает
схему:

- по `isSystemInDarkTheme()` для `SYSTEM`;
- светлую для `LIGHT`;
- тёмную для `DARK`.

Масштаб применяется через `Typography.scaled(settings.fontScale)`.
`TextStyle.scaled()` умножает `fontSize` и `lineHeight`; функция
`TextUnit.scaled()` изменяет только `sp`, не трогая другие единицы.

При явном языке тема создаёт локализованный `Context` и передаёт его через
`CompositionLocalProvider` для `LocalContext`, `LocalConfiguration` и
`LocalResources`. Поэтому строковые ресурсы Compose меняются без перезапуска
Activity.

## 11. Разбор ключевых блоков для защиты

### Блок 1. `MutableStateFlow` и read-only `StateFlow`

```kotlin
private val mutableState = MutableStateFlow(StartUiState())
val state: StateFlow<StartUiState> = mutableState.asStateFlow()
```

`MutableStateFlow` — изменяемый источник внутри ViewModel. Наружу отдаётся
`StateFlow`, поэтому UI не может напрямую записать состояние и обойти сценарий.
Activity собирает его через `collectAsStateWithLifecycle()`.

### Блок 2. `Channel` для одноразовых событий

```kotlin
private val eventChannel = Channel<StartEvent>(Channel.BUFFERED)
val events = eventChannel.receiveAsFlow()
```

Сообщение или навигация должны произойти один раз. Если хранить их в
`StateFlow`, новая рекомпозиция или поворот может повторно показать Snackbar
или открыть Activity. `Channel.BUFFERED` сохраняет событие до сбора.

### Блок 3. Activity Result вместо глобальных данных

Регистрация возвращает `String?`, настройки — `SettingsResult?`. Контракт
проверяет `RESULT_OK` и извлекает только разрешённые поля. Это явная и
типизированная навигация.

### Блок 4. Валидация до сохранения

```text
UI → ViewModel → Validation → ошибка в UiState
                      └─────► репозиторий при Valid
```

Ошибки представлены enum-ключами. UI переводит их в ресурсы. Пароль передаётся
массивом и затирается после проверки.

### Блок 5. Сеть вне главного потока

```kotlin
override suspend fun getJokes(): List<JokeCard> =
    withContext(Dispatchers.IO) { ... }
```

`suspend` позволяет ViewModel не блокировать UI. `Dispatchers.IO` подходит для
HTTP и файлов. `viewModelScope` отменяет работу при уничтожении ViewModel.

### Блок 6. Требуемый диапазон 12–20 шуток

```text
пакет 10 → merge → distinctBy(id)
повтор до трёх раз, пока размер меньше 12
итог не больше 20
```

API ограничивает `amount` десятью, поэтому один запрос не гарантирует 12
уникальных шуток. После проверки минимума результат записывается в кэш.

### Блок 7. Ошибка сети не стирает кэш

Новый кэш записывается только после успешного получения и проверки списка.
Если HTTP-запрос или JSON-разбор завершился ошибкой, запись не выполняется.

### Блок 8. Атомарная запись JSON

```text
запись в *.tmp
  → замена основного файла
  → восстановление старого при ошибке переименования
```

Для копии временный файл ещё и повторно читается до замены. Это защищает от
замены хорошей копии заведомо некорректной.

### Блок 9. Восстановление копии

Настройки:

```text
restoreBackup() → List<JokeCard>
                → replaceJokes(list) → основной кэш
```

При возврате в `ContentActivity` вызывается `load()`, и лента читает уже
восстановленный кэш. Сеть для этого не нужна.

### Блок 10. Подтверждение необратимых действий

`SettingsActivity` сначала выставляет `PendingAction`, показывает диалог и
только после нажатия подтверждения вызывает метод ViewModel. Это касается
восстановления, удаления и очистки.

### Блок 11. Состояние ошибки интерфейса

ViewModels хранят не готовый текст исключения, а `UiMessage`. UI выбирает
локализованную строку. Исключения репозитория преобразуются в `Result` или
`runCatching`, поэтому сеть и файловая ошибка не должны приводить к падению.

### Блок 12. Жизненный цикл

Все Activity наследуют `LoggedActivity`, переопределения находятся в одном
классе. `this::class.simpleName` автоматически добавляет имя экрана. Поэтому не
нужно дублировать семь методов в каждой Activity.

## 12. Сквозные сценарии

### Первый запуск и регистрация

```text
StartActivity
  → «Создать аккаунт»
  → RegistrationActivity
  → RegistrationViewModel.register()
  → Validation
  → PreferenceUserRepository.register()
  → PasswordHasher.create()
  → RESULT_OK(login)
  → StartActivity предзаполняет логин
```

### Вход

```text
StartActivity
  → StartViewModel.authenticate()
  → PreferenceUserRepository.authenticate()
  → PasswordHasher.verify()
  → StartEvent.Authenticated(login)
  → ContentActivity
  → finish() стартовой Activity
```

### Загрузка ленты

```text
ContentActivity
  → ContentViewModel.load()
  → RemoteJokeRepository.getJokes()
     ├─ есть кэш → чтение JSON
     └─ первый запуск → refreshJokes()
          → JokeAPI → JokeJsonCodec → JokeFeedPolicy
          → атомарная запись кэша
  → ContentUiState
  → ContentScreen / LazyColumn
```

### Обновление

`refresh()` явно вызывает сеть. При успехе список заменяется. При ошибке старые
карточки остаются, пользователь получает `JOKE_REFRESH_FAILED`.

### Офлайн-режим

Если кэш уже есть, `getJokes()` читает его с диска. Сеть нужна для первой
загрузки и явного обновления.

### Резервная копия

```text
SettingsActivity
  → подтверждение в AlertDialog
  → SettingsViewModel.createBackup()
  → JokeRepository.getJokes()
  → JsonBackupRepository.createBackup()
  → jokes_backup.json
  → BackupInfo в SettingsUiState
```

После очистки кэша копия остаётся. Восстановление читает её и записывает шутки
обратно в основной кэш.

### Возврат из настроек

`returnToContent()` формирует Intent с логином и `profileChanged`; Activity
завершается. `ContentActivity` принимает результат, обновляет ленту и при
необходимости профиль.

## 13. Вопросы преподавателя и короткие ответы

**Почему четыре Activity, а не одна с Compose Navigation?**

По условию лабораторной работы нужны четыре самостоятельных Activity. Для
большего приложения удобнее Single-Activity и Navigation Compose.

**Где бизнес-логика?**

Валидация — в domain, сценарии экранов — во ViewModel, получение и сохранение
данных — в репозиториях. Activity отвечает за навигацию и результаты.

**Почему `StateFlow`, а не просто изменяемые поля?**

`StateFlow` даёт одно актуальное состояние, поддерживает подписку и переживает
пересоздание Activity, потому что ViewModel сохраняется при повороте.

**Зачем `collectAsStateWithLifecycle`, а не обычный collect?**

Подписка учитывает жизненный цикл: Compose не продолжает собирать состояние,
когда Activity неактивна.

**Почему сообщения идут через `Channel`?**

Они одноразовые. `Channel` не повторяет Snackbar и навигацию после
рекомпозиции.

**Почему пароль передаётся как `CharArray`?**

Массив можно очистить после использования. Неизменяемую `String` нельзя
гарантированно затереть.

**Пароль хранится в открытом виде?**

Нет. В `SharedPreferences` сохраняются случайная соль и учебный хеш. Через
Intent передаётся только логин. Для production нужен проверенный password
hashing with KDF.

**Почему API не возвращает все шутки одним запросом?**

JokeAPI ограничивает `amount` десятью. Репозиторий делает до трёх пакетов,
дедуплицирует по `id` и ограничивает итог двадцатью.

**Что будет, если сеть пропала после обновления?**

Старый кэш остаётся, потому что запись нового выполняется только после
успешной загрузки и проверки минимум 12 шуток.

**Почему JSON-кодек отдельный?**

Чтобы `JokeCard` не зависел от `JSONObject`, а ViewModel и Compose не работали
с сырым ответом API.

**Что произойдёт при повреждённой резервной копии?**

`JsonBackupRepository` проверяет версию, JSON, непустоту и уникальность `id`.
Операция вернёт ошибку; UI получит `BACKUP_RESTORE_FAILED`.

**Какой тип заднего плана хранения выбран?**

`SharedPreferences` для небольших скалярных значений и JSON-файлы для списка
шуток и копии.

**Почему нет серверной авторизации?**

По условию достаточно локальной регистрации на одном устройстве. Серверная
аутентификация не входит в границы работы.

**Как доказать логирование жизненного цикла?**

Запустить `adb logcat -s Lab1Lifecycle` и пройти маршрут через четыре Activity.
Формат строки задаёт `LoggedActivity`; пароли и содержимое шуток не выводятся.

## 14. Короткий текст выступления

> «В проекте Punchline четыре Activity, по одной на экран: старт, регистрация,
> коллекция и настройки. Вход и регистрация работают локально. Профиль хранится
> в SharedPreferences, пароль — как соль и хеш, через Intent передаётся только
> логин. Архитектура построена по схеме MVVM с однонаправленным потоком данных:
> Compose передаёт действие во ViewModel, ViewModel проверяет данные и вызывает
> репозиторий, после чего обновляет StateFlow. Сеть и файлы работают в
> Dispatchers.IO. Шутки загружаются из JokeAPI пакетами, преобразуются
> JokeJsonCodec в JokeCard, дедуплицируются по id и сохраняются в JSON-кэш. При
> ошибке сети старый кэш не перезаписывается. Резервная копия хранит версию,
> дату и список шуток и восстанавливается без интернета. Все Activity наследуют
> LoggedActivity, поэтому жизненный цикл логируется единообразно. В Compose
> экраны получают состояние и колбэки, тема поддерживает светлый, тёмный и
> системный режим, масштаб шрифта и выбор языка».

## 15. Что показать в демонстрации

1. Первый запуск и внешний вид стартового экрана.
2. Регистрацию с ошибкой и успешную регистрацию.
3. Возврат логина на стартовый экран.
4. Неверный пароль, затем правильный вход.
5. Загрузку ленты и прокрутку до последней карточки.
6. Обновление ленты.
7. Открытие настроек системной кнопкой и toolbar.
8. Смену темы, шрифта и языка.
9. Создание копии, очистку кэша и восстановление без сети.
10. Удаление копии и неактивные кнопки после удаления.
11. Logcat с жизненным циклом четырёх Activity.
12. Исходники `RemoteJokeRepository`, `JsonBackupRepository`, ViewModel и
    `LoggedActivity`.

## 16. Честные ограничения решения

Эти формулировки полезны, если преподаватель спросит о production-качестве:

- локальная учётная запись без сервера и многопользовательского режима;
- `SharedPreferences` не шифрует профиль; пароль защищён только солью и хешем;
- хеширование учебное, а не промышленный Argon2/PBKDF2;
- JSON-файл подходит для небольшой ленты, но не заменяет базу данных;
- для проекта с историей, поиском и большим объёмом данных логичнее Room;
- для сложной навигации и долговременного многопользовательского сценария лучше
  Single-Activity с Navigation Compose и серверной авторизацией;
- текущая проверка поведения — ручные сценарии и успешная сборка; unit-тестов в
  текущей минимальной конфигурации проекта нет.

## 17. Мини-конспект «кто за что отвечает»

| Вопрос | Ответ |
|---|---|
| Кто открывает следующий экран? | Activity через Intent/Activity Result |
| Кто хранит UI-состояние? | ViewModel в `StateFlow` |
| Кто проверяет ввод? | `Validation` и ViewModel |
| Кто знает про JSON и HTTP? | `RemoteJokeRepository`, `JokeJsonCodec` |
| Кто читает и пишет preferences? | `Preference*Repository` |
| Кто создаёт и восстанавливает копию? | `JsonBackupRepository` |
| Кто создаёт объекты зависимостей? | `AppContainer` |
| Кто рисует интерфейс? | Compose-экраны и общие компоненты |
| Кто выбирает светлую/тёмную тему? | `PunchlineTheme` |
| Кто логирует lifecycle? | `LoggedActivity` |

