#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer
Feature: Customer Information and Preferences Management
  Feature to manage customer billing information such as branches and pruchase orders etc.

  Scenario: Adding new customer send notification to the customer contact and finance head
    Given I add new Customer "Minty & Sons Pvt. Ltd" with relevant details
    When I add contact information with email "foo@bar.com" to customer
    Then "foo@bar.com" receives a welcome email from "finance@agilityroots.com"
    
	Scenario: Adding braches to a customer notifies the branch contact and finance head
		Given I have customer "Minty & Sons Pvt. Ltd."
		When I add branch named "Main Branch" with all relevant details
		And I add contact information with email "branch@bar.com" to branch
		Then "branch@bar.com" receives a welcome email from "finance@agilityroots.com"  
