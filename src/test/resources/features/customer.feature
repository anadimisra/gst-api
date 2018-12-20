#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer, branch

Feature: Customer Information and Preferences Management
  Feature to manage customer billing information such as branches and pruchase orders etc.

   @functional @finance
   Scenario: Adding new customer with all relevant data sends a welcome email to customer
   Given I am allowed to add customers as "finance manager" 
   And I choose to add "new" "customer"
   When I review all information and save
   Then "M/S Stark And Gang Inc." is listed in the "customers" page
   And "tony@starkandgang.com" gets customer details email from "finance@agilityroots.com"

   @functional @finance
   Scenario: Cannot add customer branch without GSTIN and branch contact, latter can be same as customer contact
   Given I am allowed to add branches as "finance manager"
   And I want to add branch "Other Office" to "M/S Stark And Gang Inc."
   When I review all information and save
   Then I see the branch name "Other Office" is listed in customers detail page
   And "tony@starkandgang.com" gets branch details email from "accounts@agilityroots.com"
   
   @functional @accounts
   Scenario: Cannot add customer branch without GSTIN and branch contact, latter can be same as customer contact
   Given I am allowed to add branches as "accounts manager"
   And I want to add branch "Other Office" to "M/S Stark And Gang Inc."
   When I review all information and save
   Then I see the branch name "Other Office" is listed in customers detail page
   And "tony@starkandgang.com" gets branch details email from "accounts@agilityroots.com"   