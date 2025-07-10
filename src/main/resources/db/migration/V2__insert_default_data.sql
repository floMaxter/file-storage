insert into file_storage.roles(id, name)
values (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER');

insert into file_storage.users(id, username, password)
values (1, 'Max Vdovin', '$2a$12$Z858/nNOA6GCIKjfLUSVduvzNxdtDT6bZCtMc5z/tNrsK.dQ21N9S'),
       (2, 'Dima Vdovin', '$2a$12$Z858/nNOA6GCIKjfLUSVduvzNxdtDT6bZCtMc5z/tNrsK.dQ21N9S');

insert into file_storage.users_roles(user_id, role_id)
values (1, 2),
       ( 2, 2);

SELECT setval('file_storage.users_id_seq', (SELECT MAX(id) FROM file_storage.users));
SELECT setval('file_storage.roles_id_seq', (SELECT MAX(id) FROM file_storage.roles));
