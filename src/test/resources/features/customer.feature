#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer
Feature: Customer Information and Preferences Management
  Feature to manage customer billing information such as branches and pruchase orders etc.

  Scenario: Adding new customer send notification to the customer contact and finance head
    Given I add new Customer "Minty & Sons Pvt. Ltd" with relevant details
    When I add contact information with email "foo@bar.com" to the customer
    Then "foo@bar.com" recieves a welcome email from "finance@agilityroots.com"