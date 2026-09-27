-- 01_criacao.sql
-- Cria o banco `concessionaria` com as 12 tabelas do projeto, do zero.
--
-- Ordem de execucao da entrega:  01_criacao.sql  ->  02_insercao.sql  ->  03_consultas.sql
--
-- Por que existe o DROP DATABASE: este script e re-executavel. Rodar de novo reseta o
-- banco, evita "table already exists" e faz os ids AUTO_INCREMENT voltarem a comecar
-- em 1. O 02_insercao.sql depende disso: ele grava chaves estrangeiras por id numerico
-- (ex.: carro.compra_id = 1) sem informar o id no INSERT.
--
-- Por que existe o SET NAMES: o console do Windows em pt-BR trabalha em CP850. Sem esta
-- linha, o cliente mysql le o arquivo como CP850, o acento deixa de ser 1 caractere e
-- passa a ser 2, e o valor fica gravado corrompido no banco (ex.: 'Pérola' vira
-- 'P├®rola'). Com SET NAMES utf8mb4 o servidor interpreta os bytes do arquivo como
-- UTF-8, nao importa em que console o script foi executado.
--
-- O charset tambem e fixado no banco para nao depender do default do servidor.

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS concessionaria;

CREATE DATABASE concessionaria
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE concessionaria;

CREATE TABLE fornecedor (
	CNPJ CHAR(14) PRIMARY KEY,
	nome VARCHAR(100) NOT NULL,
	email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE telefone_fornecedor (
	fornecedor_CNPJ CHAR(14),
	telefone VARCHAR(20),
	PRIMARY KEY (fornecedor_CNPJ, telefone),
	FOREIGN KEY (fornecedor_CNPJ) REFERENCES fornecedor(CNPJ) ON UPDATE CASCADE 
);

CREATE TABLE compra (
	id INT PRIMARY KEY AUTO_INCREMENT,
	data DATE DEFAULT (CURRENT_DATE),
	valor DECIMAL(12,2) NOT NULL CHECK (valor > 0),
	fornecedor_CNPJ CHAR(14) NOT NULL,
	FOREIGN KEY (fornecedor_CNPJ) REFERENCES fornecedor(CNPJ)
);

CREATE TABLE cor(
	id INT PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE marca(
	id INT PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(50)
);

CREATE TABLE modelo(
	id INT PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(100),
	marca_id INT NOT NULL,
	FOREIGN KEY (marca_id) REFERENCES marca(id)
);


CREATE TABLE vendedor(
	id INT PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(100) NOT NULL, 
	comissao DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE endereco(
	id INT PRIMARY KEY AUTO_INCREMENT ,
	logradouro VARCHAR(100),
	numero VARCHAR(10),
	complemento VARCHAR(50),
	bairro VARCHAR(50),
	cidade VARCHAR(100),
	estado CHAR(2),
	CEP CHAR(8)
);

CREATE TABLE cliente(
	CPF CHAR(11) PRIMARY KEY,
	nome VARCHAR(100) NOT NULL,
	endereco_id INT NOT NULL,
	FOREIGN KEY (endereco_id) REFERENCES endereco(id)
);

CREATE TABLE telefone_cliente (
	cliente_CPF CHAR(11),
	telefone VARCHAR(20),
	PRIMARY KEY (cliente_CPF, telefone),
	FOREIGN KEY (cliente_CPF) REFERENCES cliente(CPF) ON UPDATE CASCADE
);

CREATE TABLE venda(
	id INT PRIMARY KEY AUTO_INCREMENT,
	data DATE DEFAULT (CURRENT_DATE),
	valor DECIMAL(12,2) NOT NULL CHECK (valor > 0),
	vendedor_id INT NOT NULL,
	cliente_CPF CHAR(11) NOT NULL,
	FOREIGN KEY (vendedor_id) REFERENCES vendedor(id),
	FOREIGN KEY (cliente_CPF) REFERENCES cliente(CPF)
);

CREATE TABLE carro(
	chassi CHAR(17) PRIMARY KEY,
	ano YEAR NOT NULL CHECK (ano >= 1900),
	preco DECIMAL(12,2) NOT NULL CHECK (preco > 0),
	cor_id INT NOT NULL,
	modelo_id INT NOT NULL,
	compra_id INT NOT NULL,
	venda_id INT NULL,
	FOREIGN KEY (cor_id) REFERENCES cor(id),
	FOREIGN KEY (modelo_id) REFERENCES modelo(id),
	FOREIGN KEY (compra_id) REFERENCES compra(id),
	FOREIGN KEY (venda_id) REFERENCES venda(id) ON DELETE SET NULL
);
