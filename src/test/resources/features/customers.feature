#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer
@customer
Feature: Customer Information and Preferences Management
  To manage customer billing information such as branches and pruchase orders etc.

  Scenario: Adding new customer send notification to the customer contact
    Given I add new Customer "Minty & Sons Pvt. Ltd" with relevant details
    When I add contact information with email "foo@bar.com" to customer
    Then "foo@bar.com" receives a welcome email from "finance@agilityroots.com"

  Scenario: Adding braches to a customer notifies the branch contact
    Given I have customer "Minty & Sons Pvt. Ltd."
    When I add branch named "Main Branch" with all relevant details
    And I add contact information with email "branch@bar.com" to branch
    Then "branch@bar.com" receives a welcome email from "finance@agilityroots.com"

  Scenario: Updating customer contact sends notification email to new contact
    Given I have customer "Minty & Sons Pvt. Ltd." with contact details
    When I update contact information with email "foo.new@bar.com" to customer
    Then "foo.new@bar.com" receives a welcome email from "finance@agilityroots.com"

  Scenario: Updating branch contact of a customer notifies the new branch contact
    Given I have customer "Minty & Sons Pvt. Ltd." with branch name "Main Branch"
    And I update contact information with email "branch.new@bar.com" to branch
    Then "branch.new@bar.com" receives a welcome email from "finance@agilityroots.com"