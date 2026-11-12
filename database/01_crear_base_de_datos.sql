-- ============================================================================
--  Sistema de Gestion y Seguimiento del Avance de Obras - DRACOR
--  Script para HeidiSQL (MySQL / MariaDB de XAMPP, localhost:3306)
--
--  Solo crea la base de datos vacia. Las tablas las crea el sistema solo la
--  primera vez que se ejecuta (Hibernate) y tambien carga los datos iniciales.
--  Este paso es opcional: el sistema tambien crea la base si no existe.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `dracor_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `dracor_db`;

-- Para reiniciar el sistema desde cero (borra TODOS los datos), ejecutar:
-- DROP DATABASE `dracor_db`;
