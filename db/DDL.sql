-- minIO 文件信息表
drop table if exists tb_file_info;
create table tb_file_info
(
    id          bigserial
        primary key,
    bucket      varchar(255)            not null,
    object_name varchar(255)            not null,
    path        varchar(255),
    file_name   varchar(255),
    md5         varchar(255),
    create_time timestamp default now() not null,
    update_time timestamp default now() not null
);

comment
on column tb_file_info.bucket is '桶名称';

comment
on column tb_file_info.object_name is '带路径的文件名称';

comment
on column tb_file_info.path is '路径';

comment
on column tb_file_info.file_name is '文件名称';

comment
on column tb_file_info.md5 is 'MD5值';

comment
on column tb_file_info.create_time is '创建时间';

comment
on column tb_file_info.update_time is '修改时间';

