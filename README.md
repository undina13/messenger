/users/signup - регистрация юзера  @RequestBody RegisterUser
/users/activate/{userId}/{verifyCode} - верификация кода подтверждения - приходит юзеру в письме
/users/login - @RequestBody LoginRequest вход юзера в систему
/users/logout - выход юзера из системы
/users/me -  getMapping - получение инормации о залогиненном юзере
/users/me -  patchMapping @RequestBody UpdateUser- залогиненный юзер меняет свои данные
/users/email/{newEmail}/{userId}/{verifyCode} - смена емейла залогиненным пользователем, ссылка приходит в письме, если меняется емейл в личных данных
/users/password - @RequestBody PasswordDto залогиненный юзер меняет пароль
/users/blocked/{isBlocked} - пользователь может заблокировать/разблокировать свой аккаунт (в дальнейшем эта функция будет отдана администраторам)

/messages - @RequestBody CreateMessageDto пользотватель отправляет сообщение
/messages/chat/{recipientLogin} - пользователь получает сообщения из чата с другим пользователем

дополнительно

/users/friend/{friendLogin} - @PostMapping -пользователь добавляет другого в друзья по логину. Сейчас дружба одностороняя, то есть один человек может быть подписан на другого, а второй на него нет. В дальнейшем можно реализовать двустороннюю дружбу с дополнительными функциями
/users/friend/{friendLogin} - @GetMapping - получить список друзей другого пользователя по логину

Ограничения из-за ограниченного количества времнени на тз:
Hibernate сам создает таблицы, как будет время, добавлю liquibase
добавлю Redis для хранения токенов и усовершенствую logout
не хватило времени на веб-сокеты
Пользователь не может отправить сообщение самому себе в данной реализации. В дальнейшем можно добавить сущность Chat, тогда отправка себе сообщений будет возможна.

Реализована роль администратора, сейчас администратором пользователь становится автоматически, если его емейл заканчивается определенными в application property (admin.email.domain) символами, в дальнейшем можно будет расширить проект, добавив права адиминистраторам и админскую часть 