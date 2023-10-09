INSERT INTO USERS (id, email, password, role, blocked, created, login, first_name, last_name, is_closed_friends, is_closed_messages)
VALUES ('b4861725-8aa5-4770-8e78-c9b6694dc975', 'user1@mail.ru', '$2a$10$Ept0QPESQRIcmKcTPyZgd.2ufSthHmt/f2Z5hrDiQ/BIh9AysEZ5u', 'ROLE_USER', false, '2023-10-02 19:13:10', 'Ivan', 'Ivan', 'Ivanov', false, false);
INSERT INTO USERS (id, email, password, role, blocked, created, login, first_name, last_name, is_closed_friends, is_closed_messages)
VALUES ('f09108bf-b0ff-4017-85e9-8799b702f246', 'user2@mail.ru', '$2a$10$lTgowhKpte2llERILz/C9ermIl9Q.ICoDa0ZkkLSm9dR2OeNdtKuW', 'ROLE_USER', false, '2023-10-02 19:13:10', 'Petr', 'Petr', 'Petrov', false, false);
INSERT INTO USERS (id, email, password, role, blocked, created, login, first_name, last_name, is_closed_friends, is_closed_messages)
VALUES ('d1f20549-3a36-4ab4-bce4-b1586c360587', 'admin@mail.ru', '$2a$10$lTgowhKpte2llERILz/C9ermIl9Q.ICoDa0ZkkLSm9dR2OeNdtKuW', 'ROLE_USER', false, '2023-10-02 19:13:10', 'Sidor', 'Sidor', 'Sidorov', true, true);


INSERT INTO user_friends(user_id, friend_user_id)
VALUES ('d1f20549-3a36-4ab4-bce4-b1586c360587', 'f09108bf-b0ff-4017-85e9-8799b702f246');
INSERT INTO user_friends(user_id, friend_user_id)
VALUES ('b4861725-8aa5-4770-8e78-c9b6694dc975', 'f09108bf-b0ff-4017-85e9-8799b702f246');