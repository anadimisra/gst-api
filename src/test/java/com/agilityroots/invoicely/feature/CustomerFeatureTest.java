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
@CucumberOptions(features = { "src/test/resources/features/customer.feature" }, plugin = { "pretty",
    "html:target/reports/customer/cucumber/html", "json:target/customer-cucumber.json",
    "usage:target/customer-usage.jsonx", "junit:target/customer-junit.xml" })
public class CustomerFeatureTest {

}
