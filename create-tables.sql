create table GterPerson(
	id int not null auto_increment,
    IELTS varchar(100) default "无",
    TOEFL varchar(100) default "无",
    GRE varchar(100) default "无",
    GMAT varchar(100) default "无",
    OTHER varchar(100) default "无",
    cnUniversity varchar(100) default "无",
    cnMajor varchar(100) default "无",
    cnGPA varchar(100) default "无",
    cnNote varchar(500) default "无",
    primary key(id)
);
create table GterOffer(
	id int not null auto_increment,
    personId int,
    university varchar(100) default "无",
    major varchar(100) default "无",
    result varchar(50) default "无",
    degree varchar(50) default "无",
    informDate varchar(50) default "无",
    enterDate varchar(50) default "无",
    enterSemester varchar(50) default "无",
    link varchar(250) default "无",
    primary key(id),
    foreign key(personId) references GterPerson(id)
);
create table user(
	id int not null auto_increment,
    name varchar(50),
    email varchar(50),
    password varchar(50),
    primary key (id)
);
select *
from gteroffer, gterperson
where gteroffer.personId = gterperson.id;
select * from gterperson;
select * from gteroffer;
delete from gteroffer where id<>0;
delete from gterperson where id<>0;
select * from user;
insert into user(name,email,password) values("admin","admin@gmail.com","000000");