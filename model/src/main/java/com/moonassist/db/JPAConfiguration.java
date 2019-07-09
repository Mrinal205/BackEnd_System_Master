package com.moonassist.db;


import com.google.common.collect.ImmutableSet;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.Set;

@Configuration
@EnableJpaRepositories(basePackages = "com.moonassist.persistence",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager")
@EnableTransactionManagement
public class JPAConfiguration {

  @Autowired
  private Environment environment;

  @Value("${datasource.moonassist.maxPoolSize}")
  private int maxPoolSize;

  private static final Set<String> DEPLOYED_ENVIRONMENTS = ImmutableSet.of(
      "TEST", "PRODUCTION"
  );


  /*
   * Populate SpringBoot DataSourceProperties object directly from application.yml
   * based on prefix.Thanks to .yml, Hierarchical data is mapped out of the box with matching-name
   * properties of DataSourceProperties object].
   */
  @Bean
  @Primary
  @ConfigurationProperties(prefix = "datasource.moonassist")
  public DataSourceProperties dataSourceProperties(){
    return new DataSourceProperties();
  }

  /*
   * Configure HikariCP pooled DataSource.
   */
  @Bean
  public DataSource dataSource() {
    DataSourceProperties dataSourceProperties = dataSourceProperties();
    HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder
        .create(dataSourceProperties.getClassLoader())
        .driverClassName(dataSourceProperties.getDriverClassName())
        .url(dataSourceProperties.getUrl())
        .username(dataSourceProperties.getUsername())
        .password(dataSourceProperties.getPassword())
        .type(HikariDataSource.class)
        .build();
    dataSource.setMaximumPoolSize(maxPoolSize);

    String environment = System.getenv("ENVIRONMENT");

    if (DEPLOYED_ENVIRONMENTS.contains(environment)) {
      String url = System.getenv("DATABASE_URL");
      String username = System.getenv("DATABASE_USERNAME");
      String password = System.getenv("DATABASE_PASSWORD");

      url = url.replace(username + ":" + password + "@", "");
      url = url.replace("postgres", "postgresql");
      dataSource.setJdbcUrl("jdbc:" + url);
      dataSource.setUsername(username);
      dataSource.setPassword(password);
    }

    return dataSource;
  }

  /*
   * Entity Manager Factory setup.
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
    factoryBean.setDataSource(dataSource());
    factoryBean.setPackagesToScan(new String[] { "com.moonassist.persistence" });
    factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
    factoryBean.setJpaProperties(jpaProperties());
    return factoryBean;
  }

  /*
   * Provider specific adapter.
   */
  @Bean
  public JpaVendorAdapter jpaVendorAdapter() {
    HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
    return hibernateJpaVendorAdapter;
  }

  /*
   * Here you can specify any provider specific properties.
   */
  private Properties jpaProperties() {
    Properties properties = new Properties();
    properties.put("hibernate.dialect", environment.getRequiredProperty("datasource.moonassist.hibernate.dialect"));
    properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("datasource.moonassist.hibernate.hbm2ddl.method"));
    properties.put("hibernate.show_sql", environment.getRequiredProperty("datasource.moonassist.hibernate.show_sql"));
    properties.put("hibernate.format_sql", environment.getRequiredProperty("datasource.moonassist.hibernate.format_sql"));

    if(StringUtils.isNotEmpty(environment.getRequiredProperty("datasource.moonassist.defaultSchema"))){
      properties.put("hibernate.default_schema", environment.getRequiredProperty("datasource.moonassist.defaultSchema"));
    }

    return properties;
  }

  @Bean
  @Autowired
  public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
    JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(emf);
    return txManager;
  }

}