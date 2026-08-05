--
-- PostgreSQL database dump
--

\restrict fDhhzzNvelmvn3zvHBYc8lDT8pHFwujMzCXn3hwtWSuacshX7Jahh9B0onTFDeO

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE public.audit_logs DISABLE TRIGGER ALL;



ALTER TABLE public.audit_logs ENABLE TRIGGER ALL;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.users DISABLE TRIGGER ALL;

INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (1, 'Jorge', 'Administrador', 'jorge@obraspublicas.com', 'jorge', '$2a$10$mFGm6tGhL2RCLFQgx0JyMO.Ss0rwK/u33BKVWF0vC/piivDH0USrC', true, '2026-07-03 12:23:27.621394', '2026-07-03 12:23:27.621394');
INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (2, 'Pedro', 'Supervisor', 'pedro@obraspublicas.com', 'supervisor', '$2a$10$H.KVogvF9HhYXuV3qE202.gSJNcV208ew4lx/IyWvAh2T6uQ1dk0K', true, '2026-07-03 12:23:28.078127', '2026-07-03 12:23:28.078127');
INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (3, 'Juan', 'Contratista', 'juan@obraspublicas.com', 'contratista', '$2a$10$Ga/Eo5gQXanGrM7oxHYBUuhDcrIA17me85Ufv8eNs84O7ApV07FTG', true, '2026-07-03 12:23:28.078127', '2026-07-03 12:23:28.078127');
INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (4, 'Ana', 'Auditora', 'ana@obraspublicas.com', 'auditor', '$2a$10$16WLhkYh9b1kuUgJ1/qwrOCyeu3P2O2GFJ9eKsoYuzkNtOywKu/22', true, '2026-07-03 12:23:28.078127', '2026-07-03 12:23:28.078127');
INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (5, 'nayeli', 'Administrador', 'nayeli@obraspublicas.com', 'nayeli', '$2a$10$sEIXQUy3RBoiVdLgFxzEuu.d1plxKSa3HmD4B2n6N0cfr9pdzrXny', true, '2026-07-03 12:47:11.552286', '2026-07-03 12:47:11.552286');
INSERT INTO public.users (id, first_name, last_name, email, username, password, active, created_at, updated_at) VALUES (6, 'nayeli', 'Administrador', 'nayeli2@obraspublicas.com', 'nayeli2', '$2a$10$M4gMzBA8gOhjQlk5KbABI.CJEunn8jWzLHyROJ9BRrv13sdED/yD2', true, '2026-07-03 12:53:49.919237', '2026-07-03 12:53:49.919237');


ALTER TABLE public.users ENABLE TRIGGER ALL;

--
-- Data for Name: obras; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.obras DISABLE TRIGGER ALL;



ALTER TABLE public.obras ENABLE TRIGGER ALL;

--
-- Data for Name: obra_avances; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.obra_avances DISABLE TRIGGER ALL;



ALTER TABLE public.obra_avances ENABLE TRIGGER ALL;

--
-- Data for Name: avance_evidencias; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.avance_evidencias DISABLE TRIGGER ALL;



ALTER TABLE public.avance_evidencias ENABLE TRIGGER ALL;

--
-- Data for Name: bitacoras; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.bitacoras DISABLE TRIGGER ALL;



ALTER TABLE public.bitacoras ENABLE TRIGGER ALL;

--
-- Data for Name: catalogo_documentos; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.catalogo_documentos DISABLE TRIGGER ALL;

INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (1, 'PARTE_SOCIAL', 'Acta de Integracion del Consejo de Desarrollo Municipal (Art. 33 de la L.C.F. y 17 de la L.C.F.E.O. y art. 68 fracc. XIV de la L.O.M.E.O.)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (2, 'PARTE_SOCIAL', 'Acta de Selección de Obras', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (3, 'PARTE_SOCIAL', 'Acta de Priorización de Obras, Acciones Sociales Básicas', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (4, 'PARTE_SOCIAL', 'Acta de Integración del Comité de Obras', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (5, 'PARTE_SOCIAL', 'Convenio de Concertación', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (6, 'PARTE_SOCIAL', 'Acta de Cabildo de Aprobación de la Obra.', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (7, 'PARTE_SOCIAL', 'Acta de Acuerdo de Cabildo para Ejecutar la Obra por Administracion Directa', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (8, 'PROYECTO_EJECUTIVO', 'Estudio de Factibilidad Tecnica, economica y ecologica de la realizacion de la obra', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (9, 'PROYECTO_EJECUTIVO', 'Oficio de Notificación, Aprobación y Autorización de Obras', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (10, 'PROYECTO_EJECUTIVO', 'Anexo del Oficio de Notificación, Aprobación y Autorización de Obras', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (11, 'PROYECTO_EJECUTIVO', 'Cedula de Inf. Basica', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (12, 'PROYECTO_EJECUTIVO', 'Generalidades de la Inversion', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (13, 'PROYECTO_EJECUTIVO', 'Documentos que Acrediten la Tenencia de la Tierra', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (14, 'PROYECTO_EJECUTIVO', 'Dictamen de Impacto Ambiental', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (15, 'PROYECTO_EJECUTIVO', 'Presupuesto de Obra (Catalogo de Conceptos)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (16, 'PROYECTO_EJECUTIVO', 'Presupuesto de Obra (Explosion de Insumos)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (17, 'PROYECTO_EJECUTIVO', 'Generadores de Obra Programada', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (18, 'PROYECTO_EJECUTIVO', 'Planos de Proyecto', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (19, 'PROYECTO_EJECUTIVO', 'Especificaciones Generales y Particulares', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (20, 'PROYECTO_EJECUTIVO', 'Firma del Director Responsable de Obra', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (21, 'PROYECTO_EJECUTIVO', 'Programa de Obra e Inversion', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (22, 'PROYECTO_EJECUTIVO', 'Croquis de Microlocalización', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (23, 'PROYECTO_EJECUTIVO', 'Croquis de Macrolocalización', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (24, 'DOCUMENTACION_COMPROBATORIA', 'Inventario de la maquinaria y equipo de construccion con que cuenta el municipio (art. 8 fracc. I y 62 de la L.O.P.S.R.E.O.)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (25, 'DOCUMENTACION_COMPROBATORIA', 'Relacion de la plantilla del Personal Tecnico y Administrativo relacionado con la obra (art. 8 fracc. III y 62 de la L.O.P.S.R.E.O.)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (26, 'DOCUMENTACION_COMPROBATORIA', 'Documentacion que soporta el pago de Mano de Obra (Listas de Raya)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (27, 'DOCUMENTACION_COMPROBATORIA', 'Identificacion Oficial de los Trabajadores que Aparecen en las listas de Raya', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (28, 'DOCUMENTACION_COMPROBATORIA', 'Documentacion que soporta la Compra de Materiales (Facturas)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (29, 'DOCUMENTACION_COMPROBATORIA', 'Reporte Fotografico', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (30, 'DOCUMENTACION_COMPROBATORIA', 'Notas de Bitacora de Obra', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (31, 'DOCUMENTACION_COMPROBATORIA', 'Convocatoria', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (32, 'DOCUMENTACION_COMPROBATORIA', 'Contrato de Arrendamiento de Maquinaria (art. 63 de la L.O.P.S.R.E.O)', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (33, 'DOCUMENTACION_COMPROBATORIA', 'Acta de entrega Recepcion', true, true);
INSERT INTO public.catalogo_documentos (id, seccion, nombre, requerido, activo) VALUES (34, 'DOCUMENTACION_COMPROBATORIA', 'Cedula Detallada de la facturacion total de la obra.', true, true);


ALTER TABLE public.catalogo_documentos ENABLE TRIGGER ALL;

--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.databasechangelog DISABLE TRIGGER ALL;

INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-001-create-permissions', 'jorge', 'db/changelog/changes/001-create-permissions.xml', '2026-07-03 12:23:27.536745', 1, 'EXECUTED', '9:f14a34d71ef84a01ab862a69f32fda86', 'createTable tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; in...', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-002-create-roles', 'jorge', 'db/changelog/changes/002-create-roles.xml', '2026-07-03 12:23:27.609605', 2, 'EXECUTED', '9:bdb5bc6fb84373a17165ab80f44edf21', 'createTable tableName=roles; insert tableName=roles; insert tableName=roles; insert tableName=roles; insert tableName=roles', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-003-create-users', 'jorge', 'db/changelog/changes/003-create-users.xml', '2026-07-03 12:23:27.671347', 3, 'EXECUTED', '9:fb08e114a9906dc3d677cafa5b2b6a28', 'createTable tableName=users; insert tableName=users', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-004-create-role-permissions', 'jorge', 'db/changelog/changes/004-create-role-permissions.xml', '2026-07-03 12:23:27.911277', 4, 'EXECUTED', '9:3304ec5185d7fded066e0a86817b6287', 'createTable tableName=role_permissions; addPrimaryKey constraintName=pk_role_permissions, tableName=role_permissions; addForeignKeyConstraint baseTableName=role_permissions, constraintName=fk_role_permissions_role, referencedTableName=roles; addFo...', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-005-create-user-roles', 'jorge', 'db/changelog/changes/005-create-user-roles.xml', '2026-07-03 12:23:27.970245', 5, 'EXECUTED', '9:8826f93db82202ff9280de111fd6a6c5', 'createTable tableName=user_roles; addPrimaryKey constraintName=pk_user_roles, tableName=user_roles; addForeignKeyConstraint baseTableName=user_roles, constraintName=fk_user_roles_user, referencedTableName=users; addForeignKeyConstraint baseTableNa...', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-006-create-audit-logs', 'jorge', 'db/changelog/changes/006-create-audit-logs.xml', '2026-07-03 12:23:28.015903', 6, 'EXECUTED', '9:e092bc70e17ff197257883d38f8c9dcf', 'createTable tableName=audit_logs', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260629-007-create-bitacoras', 'jorge', 'db/changelog/changes/007-create-bitacoras.xml', '2026-07-03 12:23:28.068301', 7, 'EXECUTED', '9:0a0d992237626f25a742cea485687e94', 'createTable tableName=bitacoras; addForeignKeyConstraint baseTableName=bitacoras, constraintName=fk_bitacoras_user, referencedTableName=users', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260630-008-seed-test-users', 'antigravity', 'db/changelog/changes/008-seed-test-users.xml', '2026-07-03 12:23:28.127418', 8, 'EXECUTED', '9:545a3dacf5bbb937853957d253129b99', 'insert tableName=users; insert tableName=user_roles; insert tableName=users; insert tableName=user_roles; insert tableName=users; insert tableName=user_roles', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260630-009-create-obras', 'antigravity', 'db/changelog/changes/009-create-obras.xml', '2026-07-03 12:23:28.195294', 9, 'EXECUTED', '9:4d1cd19e29802e178b662ee62d5b3e66', 'createTable tableName=obras; addForeignKeyConstraint baseTableName=obras, constraintName=fk_obras_responsable, referencedTableName=users; addForeignKeyConstraint baseTableName=bitacoras, constraintName=fk_bitacoras_obra, referencedTableName=obras', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260630-010-create-triggers', 'antigravity', 'db/changelog/changes/010-create-triggers.xml', '2026-07-03 12:23:28.527689', 10, 'EXECUTED', '9:5be5018091f9561dbc934f1b12dec2f7', 'sql', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260630-011-seed-obra-permissions', 'antigravity', 'db/changelog/changes/011-seed-obra-permissions.xml', '2026-07-03 12:23:28.626979', 11, 'EXECUTED', '9:b527289351723437750aa591ebea66ed', 'insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=role_permissions; insert tableName=role_permissions; insert tableName=role_permissions; insert tableName=role_...', '', NULL, '4.29.2', NULL, NULL, '3103007243');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260708-012-create-catalogo-documentos', 'jorge', 'db/changelog/changes/012-create-catalogo-documentos.xml', '2026-07-08 16:02:18.712793', 12, 'EXECUTED', '9:fed5055a759a48b2b1e4e64e03d962d6', 'createTable tableName=catalogo_documentos; insert tableName=catalogo_documentos; insert tableName=catalogo_documentos; insert tableName=catalogo_documentos; insert tableName=catalogo_documentos; insert tableName=catalogo_documentos; insert tableNa...', '', NULL, '4.29.2', NULL, NULL, '3548138440');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260708-013-create-expedientes-obras', 'jorge', 'db/changelog/changes/013-create-expedientes-obras.xml', '2026-07-08 16:02:18.757744', 13, 'EXECUTED', '9:c97eed79a86de3f9082a63e9a456eb6e', 'createTable tableName=expedientes_obras; addUniqueConstraint constraintName=uc_expedientes_obras_unica, tableName=expedientes_obras', '', NULL, '4.29.2', NULL, NULL, '3548138440');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260715-014-create-geocercas', 'kevin', 'db/changelog/changes/014-create-geocercas.xml', '2026-08-03 10:01:30.406072', 14, 'EXECUTED', '9:018b0b50cb8c8004d707f084b3b3dda2', 'createTable tableName=geocercas; addForeignKeyConstraint baseTableName=geocercas, constraintName=fk_geocercas_obra, referencedTableName=obras; createTable tableName=geocerca_puntos; addForeignKeyConstraint baseTableName=geocerca_puntos, constraint...', '', NULL, '4.29.2', NULL, NULL, '5772890155');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260715-015-create-rutas-obras', 'kevin', 'db/changelog/changes/015-create-rutas-obras.xml', '2026-08-03 10:01:30.453996', 15, 'EXECUTED', '9:71722f64eaec6613366c7bd55a4eacbd', 'createTable tableName=rutas_obras; addForeignKeyConstraint baseTableName=rutas_obras, constraintName=fk_rutas_obras_obra, referencedTableName=obras; createTable tableName=ruta_puntos; addForeignKeyConstraint baseTableName=ruta_puntos, constraintNa...', '', NULL, '4.29.2', NULL, NULL, '5772890155');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260715-016-seed-geocerca-ruta-permissions', 'kevin', 'db/changelog/changes/016-seed-geocerca-ruta-permissions.xml', '2026-08-03 10:01:30.492026', 16, 'EXECUTED', '9:3b8fc57367fc2398cc7572651702dfe8', 'insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions; insert tableName=permissions', '', NULL, '4.29.2', NULL, NULL, '5772890155');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260803-017-add-geolocalizacion-to-obras', 'jorge', 'db/changelog/changes/017-add-geolocalizacion-to-obras.xml', '2026-08-03 11:33:35.851742', 17, 'EXECUTED', '9:e685657693c250a9a6c73cf96bf5cd5a', 'addColumn tableName=obras', '', NULL, '4.29.2', NULL, NULL, '5778415738');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260803-018-create-obra-archivos', 'jorge', 'db/changelog/changes/018-create-obra-archivos.xml', '2026-08-03 11:33:35.980684', 18, 'EXECUTED', '9:593c02ea2a0427d84f57d13324cebca1', 'createTable tableName=obra_archivos', '', NULL, '4.29.2', NULL, NULL, '5778415738');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260803-019-create-obra-avances', 'jorge', 'db/changelog/changes/019-create-avances-evidencias.xml', '2026-08-03 11:33:36.005446', 19, 'EXECUTED', '9:c00eaf36b2dbcaceb3abb54ab4acc4d0', 'createTable tableName=obra_avances', '', NULL, '4.29.2', NULL, NULL, '5778415738');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('20260803-020-create-avance-evidencias', 'jorge', 'db/changelog/changes/019-create-avances-evidencias.xml', '2026-08-03 11:33:36.028665', 20, 'EXECUTED', '9:9fc580905b77db94191ef43ad5f28fc5', 'createTable tableName=avance_evidencias', '', NULL, '4.29.2', NULL, NULL, '5778415738');


ALTER TABLE public.databasechangelog ENABLE TRIGGER ALL;

--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.databasechangeloglock DISABLE TRIGGER ALL;

INSERT INTO public.databasechangeloglock (id, locked, lockgranted, lockedby) VALUES (1, false, NULL, NULL);


ALTER TABLE public.databasechangeloglock ENABLE TRIGGER ALL;

--
-- Data for Name: expedientes_obras; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.expedientes_obras DISABLE TRIGGER ALL;



ALTER TABLE public.expedientes_obras ENABLE TRIGGER ALL;

--
-- Data for Name: geocercas; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.geocercas DISABLE TRIGGER ALL;



ALTER TABLE public.geocercas ENABLE TRIGGER ALL;

--
-- Data for Name: geocerca_puntos; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.geocerca_puntos DISABLE TRIGGER ALL;



ALTER TABLE public.geocerca_puntos ENABLE TRIGGER ALL;

--
-- Data for Name: obra_archivos; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.obra_archivos DISABLE TRIGGER ALL;



ALTER TABLE public.obra_archivos ENABLE TRIGGER ALL;

--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.permissions DISABLE TRIGGER ALL;

INSERT INTO public.permissions (id, name, description) VALUES (1, 'USER_VIEW', 'Permite ver la lista de usuarios y detalles');
INSERT INTO public.permissions (id, name, description) VALUES (2, 'USER_CREATE', 'Permite registrar nuevos usuarios');
INSERT INTO public.permissions (id, name, description) VALUES (3, 'USER_UPDATE', 'Permite editar usuarios existentes');
INSERT INTO public.permissions (id, name, description) VALUES (4, 'USER_DELETE', 'Permite desactivar o eliminar usuarios');
INSERT INTO public.permissions (id, name, description) VALUES (5, 'ROLE_VIEW', 'Permite ver la lista de roles y sus permisos');
INSERT INTO public.permissions (id, name, description) VALUES (6, 'ROLE_CREATE', 'Permite crear nuevos roles');
INSERT INTO public.permissions (id, name, description) VALUES (7, 'ROLE_UPDATE', 'Permite modificar roles y asignar permisos');
INSERT INTO public.permissions (id, name, description) VALUES (8, 'ROLE_DELETE', 'Permite eliminar roles');
INSERT INTO public.permissions (id, name, description) VALUES (9, 'BITACORA_VIEW', 'Permite ver bitácoras de obras');
INSERT INTO public.permissions (id, name, description) VALUES (10, 'BITACORA_CREATE', 'Permite registrar entradas en bitácoras');
INSERT INTO public.permissions (id, name, description) VALUES (11, 'BITACORA_UPDATE', 'Permite editar entradas de bitácoras');
INSERT INTO public.permissions (id, name, description) VALUES (12, 'BITACORA_DELETE', 'Permite eliminar bitácoras');
INSERT INTO public.permissions (id, name, description) VALUES (13, 'AUDIT_VIEW', 'Permite consultar el log de auditoría del sistema');
INSERT INTO public.permissions (id, name, description) VALUES (14, 'OBRA_VIEW', 'Permite ver la lista de obras y sus detalles');
INSERT INTO public.permissions (id, name, description) VALUES (15, 'OBRA_CREATE', 'Permite registrar nuevas obras');
INSERT INTO public.permissions (id, name, description) VALUES (16, 'OBRA_UPDATE', 'Permite editar información de obras existentes');
INSERT INTO public.permissions (id, name, description) VALUES (17, 'OBRA_CHANGE_STATUS', 'Permite cambiar el estatus de las obras');
INSERT INTO public.permissions (id, name, description) VALUES (18, 'GEOCERCA_CREATE', 'Permiso para crear geocercas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (19, 'GEOCERCA_VIEW', 'Permiso para consultar geocercas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (20, 'GEOCERCA_UPDATE', 'Permiso para actualizar geocercas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (21, 'GEOCERCA_DELETE', 'Permiso para eliminar geocercas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (22, 'RUTA_CREATE', 'Permiso para crear rutas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (23, 'RUTA_VIEW', 'Permiso para consultar rutas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (24, 'RUTA_UPDATE', 'Permiso para actualizar rutas de obras');
INSERT INTO public.permissions (id, name, description) VALUES (25, 'RUTA_DELETE', 'Permiso para eliminar rutas de obras');


ALTER TABLE public.permissions ENABLE TRIGGER ALL;

--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.roles DISABLE TRIGGER ALL;

INSERT INTO public.roles (id, name, description, active) VALUES (1, 'ADMINISTRADOR', 'Administrador total del sistema', true);
INSERT INTO public.roles (id, name, description, active) VALUES (2, 'SUPERVISOR', 'Supervisor de obras y bitácoras', true);
INSERT INTO public.roles (id, name, description, active) VALUES (3, 'CONTRATISTA', 'Contratista asignado a obras específicas', true);
INSERT INTO public.roles (id, name, description, active) VALUES (4, 'AUDITOR', 'Auditor de bitácoras y logs del sistema', true);


ALTER TABLE public.roles ENABLE TRIGGER ALL;

--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.role_permissions DISABLE TRIGGER ALL;

INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 4);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 5);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 6);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 7);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 8);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 9);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 10);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 11);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 12);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 13);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 1);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 9);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 10);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 11);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (3, 9);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (3, 10);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (3, 11);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (4, 9);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (4, 13);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 14);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 15);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 16);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (1, 17);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 14);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 16);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (2, 17);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (3, 14);
INSERT INTO public.role_permissions (role_id, permission_id) VALUES (4, 14);


ALTER TABLE public.role_permissions ENABLE TRIGGER ALL;

--
-- Data for Name: rutas_obras; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.rutas_obras DISABLE TRIGGER ALL;



ALTER TABLE public.rutas_obras ENABLE TRIGGER ALL;

--
-- Data for Name: ruta_puntos; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.ruta_puntos DISABLE TRIGGER ALL;



ALTER TABLE public.ruta_puntos ENABLE TRIGGER ALL;

--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.user_roles DISABLE TRIGGER ALL;

INSERT INTO public.user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO public.user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO public.user_roles (user_id, role_id) VALUES (3, 3);
INSERT INTO public.user_roles (user_id, role_id) VALUES (4, 4);
INSERT INTO public.user_roles (user_id, role_id) VALUES (5, 1);
INSERT INTO public.user_roles (user_id, role_id) VALUES (6, 2);


ALTER TABLE public.user_roles ENABLE TRIGGER ALL;

--
-- Name: audit_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.audit_logs_id_seq', 1, false);


--
-- Name: avance_evidencias_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.avance_evidencias_id_seq', 1, false);


--
-- Name: bitacoras_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bitacoras_id_seq', 1, false);


--
-- Name: catalogo_documentos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.catalogo_documentos_id_seq', 34, true);


--
-- Name: expedientes_obras_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.expedientes_obras_id_seq', 1, false);


--
-- Name: geocerca_puntos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.geocerca_puntos_id_seq', 1, false);


--
-- Name: geocercas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.geocercas_id_seq', 1, false);


--
-- Name: obra_archivos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.obra_archivos_id_seq', 1, false);


--
-- Name: obra_avances_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.obra_avances_id_seq', 1, false);


--
-- Name: obras_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.obras_id_seq', 1, false);


--
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permissions_id_seq', 25, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 4, true);


--
-- Name: ruta_puntos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ruta_puntos_id_seq', 1, false);


--
-- Name: rutas_obras_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rutas_obras_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 6, true);


--
-- PostgreSQL database dump complete
--

\unrestrict fDhhzzNvelmvn3zvHBYc8lDT8pHFwujMzCXn3hwtWSuacshX7Jahh9B0onTFDeO

