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

Пользователь не может отправить сообщение самому себе

Реализована роль администратора, сейчас администратором пользователь становится автоматически, если его емейл заканчивается определенными в application property (admin.email.domain) символами, в дальнейшем можно будет расширить проект, добавив права адиминистраторам и админскую часть 