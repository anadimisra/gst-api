#Author: anadi.misra@agilityroots.com
#Keywords Summary : customer
@customer
Feature: Customer Information and Preferences Management
Feature to manage customer billing information such as branches and pruchase orders etc.

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

  Scenario: Invoicing a customer also shows the invoice in due invoices listing
    Given I have customer "Minty & Sons Pvt. Ltd." with branch name "Main Branch"
    When I raise invoice "MNT-AR/1" to customer
    Then due invoices listing contains invoice number "MNT-AR/1"

  Scenario: Due invoices are sorted on approaching due date
    Given I have customer "Minty & Sons Pvt. Ltd." with branch name "Main Branch"
    When the customer has 3 due invoices
    Then invoice with "least" number of days till due is the "first" in list of "due" invoices
    And invoice with "most" number of days till due is the "last" in list of "due" invoices

  Scenario: Overdue invoices are sorted on ascending due date
    Given I have customer "Minty & Sons Pvt. Ltd." with branch name "Main Branch"
    When the customer has 3 overdue invoices
    Then invoice with "least" number of days since overdue is the "last" in list of "overdue" invoices
    And invoice with "most" number of days since overdue is the "first" in list of "overdue" invoices

  Scenario: Paid invoices are sorted descending on payment date
    Given I have customer "Minty & Sons Pvt. Ltd." with branch name "Main Branch"
    When the customer has 3 paid invoices
    Then invoice with "most" recent payment date is "first" in list of "paid" invoices
    And invoice with "least" recent payment date is "last" in list of "paid" invoices