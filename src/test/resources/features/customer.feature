#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer, branch

Feature: Customer Information and Preferences Management
  Feature to manage customer billing information such as branches and pruchase orders etc.

   @functional @smoke @finance
   Scenario: Adding new customer with all relevant data sends a welcome email to customer
   Given I am 'finance' 'manager' 
   And I choose to add 'new' 'customer'
   And I am not allowed to create customer without name
   And I am not allowed to create customer wihtout address
   And I am not allowed to create customer without contact name
   And I am not allowed to create customer without contact email
   And I am allowed to add an optional invoice prefix
   And I am allowed to add an optional invoice series
   When I review all information and save
   Then 'M/S Stark And Gang Inc.' is listed in the 'customers' page
   And 'tony@starkandgang.com' gets customer details email from 'finance@agilityroots.com'

   @functional @smoke @finance @accounts
   Scenario: Cannot add customer branch without GSTIN and branch contact, latter can be same as customer contact
   Given I am 'finance' 'manager'
   And I want to add new branch to 'M/S Stark And Gang Inc.'
   And I and not allowed to create branch without GSTIN
   And I can choose contact from company contacts
   And I am not allowed to create branch without address
   And I am not allowed to create branch without branch name
   And I optionally mark this branch as default billable branch
   When I review all information and save
   Then I see the branch name is listed in customers detail page
   And 'tony@starkandgang.com' gets branch details email from 'accounts@agilityroots.com'

   Scenario: Can add multiple purchase orders to a customer with billed to and optional shipped to branches
   Given I am 'accounts' 'manager'
   And I want to add new purchase order to 'M/S Stark And Gang Inc.'
   And I am not allowed to create purchase order without id
   And I am not allowed to create purchase order without billed to address
   And I can add an optional shipped to address
   And I am not allowed to create purchase order without gorss amount
   And I am not allowed to create purchase order without valid till date
   And I review all information and save
   Then I see the purchase order is listed in the customers detail page