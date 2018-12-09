DROP TABLE IF EXISTS "enlace";
DROP TABLE IF EXISTS "rmc";
DROP TABLE IF EXISTS "kit";
DROP TABLE IF EXISTS "material";
DROP TABLE IF EXISTS "categoria";
DROP TABLE IF EXISTS "pedido";
DROP TABLE IF EXISTS "profesor";
DROP TABLE IF EXISTS "usuario";

CREATE TABLE "usuario" (
  "id" serial,
  "nombre" character varying(255) NOT NULL,
  "correo" character varying(255) NOT NULL UNIQUE,
  "nombre_usuario" character varying(255) NOT NULL UNIQUE,
  "contrasena" character varying NOT NULL,
  "foto" text,
  "rol" integer NOT NULL,
  "activo" boolean NOT NULL,
  "fecha_registro" timestamp without time zone NOT NULL,
  "ultimo_acceso" timestamp without time zone,
  "hash_confirmacion" character varying NOT NULL,
  "ultima_actualizacion" timestamp without time zone,
  PRIMARY KEY ("id")
);
CREATE INDEX "usuario_AK" ON  "usuario" ("correo");
CREATE INDEX "usuario_usuario_AK" ON  "usuario" ("nombre_usuario");

CREATE TABLE "profesor" (
  "id" serial,
  "numero" character varying(255) NOT NULL UNIQUE,
  "usuario" integer NOT NULL UNIQUE REFERENCES usuario (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  "validado" boolean,
  PRIMARY KEY ("id")
);
CREATE INDEX "profesor_AK" ON  "profesor" ("numero");
CREATE INDEX "profesor_FK,profesor_AK" ON  "profesor" ("usuario");

CREATE TABLE "pedido" (
  "id" serial,
  "usuario" integer NOT NULL REFERENCES usuario (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  "estado" character varying(255) NOT NULL,
  "fecha_pedido" timestamp without time zone NOT NULL,
  "fecha_despacho" timestamp without time zone,
  "fecha_entrega" timestamp without time zone,
  PRIMARY KEY ("id")
);
CREATE INDEX "pedido_FK" ON  "pedido" ("usuario");

CREATE TABLE "categoria" (
  "id" serial,
  "nombre" character varying(255) NOT NULL UNIQUE,
  "padre" integer REFERENCES categoria (id)
  ON DELETE SET NULL ON UPDATE CASCADE,
  PRIMARY KEY ("id")
);
CREATE INDEX "categoria_AK" ON  "categoria" ("nombre");
CREATE INDEX "categoria_FK" ON  "categoria" ("padre");

CREATE TABLE "material" (
  "id" serial,
  "nombre" character varying(255) NOT NULL,
  "descripcion" text NOT NULL,
  "foto" text,
  "estado" character varying(255) NOT NULL,
  "pedido" integer REFERENCES "pedido" (id)
  ON DELETE SET NULL ON UPDATE CASCADE,
  PRIMARY KEY ("id")
);
CREATE INDEX "FK" ON  "material" ("pedido");

CREATE TABLE "kit" (
  "id" serial,
  "titulo" character varying(255) NOT NULL,
  "descripcion" text,
  "profesor" integer NOT NULL REFERENCES usuario (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY ("id")
);
CREATE INDEX "profesor_FK" ON  "kit" ("profesor");

CREATE TABLE "rmc" (
  "id" serial,
  "material" integer NOT NULL REFERENCES "material" (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  "categoria" integer NOT NULL REFERENCES "categoria" (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY ("id")
);
CREATE INDEX "rmc_FK" ON  "rmc" ("material", "categoria");

CREATE TABLE "enlace" (
  "id" serial,
  "titulo" character varying(255),
  "url" character varying NOT NULL,
  "kit" integer NOT NULL REFERENCES "kit" (id)
  ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY ("id")
);
CREATE INDEX "enlace_FK" ON  "enlace" ("kit");

INSERT INTO usuario (nombre,correo,nombre_usuario,contrasena,rol,activo,fecha_registro,ultimo_acceso,hash_confirmacion,ultima_actualizacion)
VALUES(
'Administrador','admin@ciencias.unam.mx','admin','$2a$10$mvr5/Gz5B2.vTbdCKyo.6eGah6SJiNqvKZYlPbDCvTyR8ZWTSlBTe',1,True,'2018-12-04 09:57:52.808','2018-12-04 12:29:45.629','$2a$10$UbK4DpM6znBzTX1HOH6L3.OV7sQa8h3KiSEj0ke1c526JrznwTmSq','2018-12-04 09:57:52.808'
)
