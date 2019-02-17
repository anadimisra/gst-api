/**
 * 
 */
package com.agilityroots.invoicely.feature;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * @author anadi
 *
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features/customers.feature" }, plugin = { "pretty",
    "html:target/reports/customers/cucumber/html", "json:target/customers-cucumber.json",
    "usage:target/customers-usage.jsonx", "junit:target/customers-junit.xml" })
public class CustomerFeatureTest {

}
