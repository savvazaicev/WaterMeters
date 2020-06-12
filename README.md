# Водосчётчики

Это приложение было создано для компании по поверке водосчётчиков.

На первом экране происходит авторизоваться по email и паролю.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/1.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

После чего из Firebase Realtime Database загружается список счётчиков и показывается в списке.
Данные кешируются в Room Persistance Database, чтобы при следующем запуске не приходилось ждать загрузку.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/2.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

Необходимый счётчик можно найти в списке с помощью поиска.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/3.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

По клику на счётчик или же на кнопку "+" открывется форма с новым клиентом и заполненым номером в реестре (если счётчик был выбран из списка).

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/4.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

Чтобы быстрее заполнить поле с датой, по клику выводится диалог выбора даты.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/5.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

Также есть выпадающий список для быстрого заполнения.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/6.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

Ещё можно добавить к форме фото с камеры или из галереи.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/7.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

При отправке формы кнопка добавления блокируется и показывается прогресс загрузки.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/8.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

По окончанию загрузки форма закрывается, показывается анимация и сообщение о результате отправки.

<img src="https://github.com/savvazaicev/WaterMeters/blob/master/screenshots/9.png" data-canonical-src="https://gyazo.com/eb5c5741b6a9a16c692170a41a49c858.png" width="270" height="480" />

Используемые технологии:
* Kotlin + Coroutines
* Room Persistence
* Firebase Authentification, Realtime Database, Storage, Crashlytics
* View Binding
* RecyclerView + DiffUtil
* SharedPreferences
* Material Design

# Лицензия

> Designed and developed by 2020 Saveli Zaitsau
> 
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
> 
>    http://www.apache.org/licenses/LICENSE-2.0
> 
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
