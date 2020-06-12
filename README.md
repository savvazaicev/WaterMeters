# Водосчётчики

Это приложение было создано для компании по поверке водосчётчиков.

На первом экране происходит авторизоваться по email и паролю,
1
после чего из Firebase Realtime Database загружается список счётчиков и показывается в списке.
Данные кешируются в Room Persistance Database, чтобы при следующем запуске не приходилось ждать загрузку.
2
Необходимый счётчик можно найти в списке с помощью поиска.
3
По клику на счётчик или же на кнопку "+" открывется форма с новым клиентом и заполненым номером в реестре (если счётчик был выбран из списка).
4
Чтобы быстрее заполнить поле с датой, по клику выводится диалог выбора даты.
5
Также есть выпадающий список для быстрого заполнения.
6
Ещё можно добавить к форме фото с камеры или из галереи.
7
При отправке формы кнопка добавления блокируется и показывается прогресс загрузки.
8
По окончанию загрузки форма закрывается, показывается анимация и сообщение о результате отправки
9

Используемые технологии:
Kotlin + Coroutines
Room Persistence
Firebase Authentification, Realtime Database, Storage, Crashlytics
View Binding
RecyclerView + DiffUtil
SharedPreferences

MediaStore
ArrayAdapter
Material Design
DatePicker
SearchView
ProgressBar

# Лицензия

Designed and developed by 2020 Saveli Zaitsau

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.