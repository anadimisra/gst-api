#Author: anadi.misra@agilityroots.com
#Keywords Summary : invoice

@invoice
Feature: GST Invoices 

@functional @smoke @finance
Scenario: Adding invoice for a customer notifies customer with invoice softcopy
Given I am allowed to add invoices as a 'finance manager'
And I choose customer 'Minty & Sons Pvt. Ltd.'
And I choose purchase order 'PO-12345'
And relevant details are populated in the invoice form
When I review information and save
Then I see invoice number 'INV-20180918' in invoices list
And an email is sent to 'email@minty.com' with invoice attached