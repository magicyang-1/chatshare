package com.aiplatform.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null || name.getText() == null) {
            return name;
        }
        String processedText = addUnderscores(name.getText());
        if (processedText == null || processedText.isEmpty()) {
            return name;
        }
        return new Identifier(processedText, name.isQuoted());
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null || name.getText() == null) {
            return name;
        }
        String processedText = addUnderscores(name.getText());
        if (processedText == null || processedText.isEmpty()) {
            return name;
        }
        return new Identifier(processedText, name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null || name.getText() == null) {
            return name;
        }
        String processedText = addUnderscores(name.getText());
        if (processedText == null || processedText.isEmpty()) {
            return name;
        }
        return new Identifier(processedText, name.isQuoted());
    }

    private String addUnderscores(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (Character.isLowerCase(buf.charAt(i - 1)) &&
                Character.isUpperCase(buf.charAt(i)) &&
                Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }
} 