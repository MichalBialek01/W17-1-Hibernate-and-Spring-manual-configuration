package org.configuration;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.hibernate.cfg.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

@Configuration
@AllArgsConstructor
@EnableJpaRepositories(basePackageClasses = JpaRepositoryMarker.class)
@PropertySource({"classpath:database.propertires"})
@EnableTransactionManagement
@EnableWebMvc
@ComponentScan(basePackageClasses = ComponentScanMarker.class)
public class ApplicationConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private final org.springframework.core.env.Environment environment;

    @Setter
    private ApplicationContext appApplicationContext;

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
                = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean().setDataSource(dataSource());
        entityManagerFactoryBean().setPackagesToScan(_EntityMarker.class.getPackageName());
        entityManagerFactoryBean().setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean().setJpaProperties(jpaProperties());
        return entityManagerFactoryBean();
    }

    final Properties jpaProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty(Environment.HBM2DDL_AUTO, environment.getProperty(Environment.HBM2DDL_AUTO));
        hibernateProperties.setProperty(Environment.DIALECT, environment.getProperty(Environment.DIALECT));
        hibernateProperties.setProperty(Environment.SHOW_SQL, environment.getProperty(Environment.SHOW_SQL));
        hibernateProperties.setProperty(Environment.FORMAT_SQL, environment.getProperty(Environment.FORMAT_SQL));
        return hibernateProperties;
    }
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty(Environment.DRIVER)));
        driverManagerDataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty(Environment.DRIVER)));
        driverManagerDataSource.setDriverClassName((environment.getProperty(Environment.USER)));
        driverManagerDataSource.setDriverClassName((environment.getProperty(Environment.PASS)));
        return driverManagerDataSource;
    }
    @Bean
    public PlatformTransactionManager transactionManager(
            final EntityManagerFactory entityManagerFactory
        ){
            final JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
            jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
            return jpaTransactionManager;
        }
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslator(){
        return new PersistenceExceptionTranslationPostProcessor();
    }
    @Bean(initMethod = "migrate")
    Flyway flyway(){
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setBaselineOnMigrate(true);
        configuration.setLocations(new Location("classpath:flyway/migrations"));
        configuration.setDataSource(dataSource());
        return new Flyway(configuration);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
     registry.addResourceHandler("/resources/**").addResourceLocations("/resources");
    }
    @Bean
    public SpringResourceTemplateResolver templateResolver(){
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.appApplicationContext);
        templateResolver.setPrefix("/WEB-INF/templates");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return templateResolver;
    }
    @Bean
    public SpringTemplateEngine templateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
    @Bean
    public ThymeleafViewResolver viewResolver(){
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return resolver;
    }


}
