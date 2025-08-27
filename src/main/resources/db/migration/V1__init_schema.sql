create schema if not exists file_storage;

create table file_storage.users
(
    id       serial primary key,
    username varchar not null check ( length(trim(username)) > 0),
    password varchar
);
create unique index idx_users_username on file_storage.users (username);

create table file_storage.roles
(
    id        serial primary key,
    name varchar not null check ( length(trim(name)) > 0)
);
create unique index ids_roles_name on file_storage.roles (name);

create table file_storage.users_roles
(
    user_id int not null references file_storage.users(id),
    role_id int not null references file_storage.roles(id),
    constraint pk_users_roles primary key (user_id, role_id)
);