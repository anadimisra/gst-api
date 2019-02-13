/**
 *  20-Nov-2018 FeatureTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
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
@CucumberOptions(features = { "src/test/resources/features" }, plugin = { "pretty", "html:target/reports/cucumber/html",
    "json:target/cucumber.json", "usage:target/usage.jsonx", "junit:target/junit.xml" })
public class FeatureTests {

}
