# 2. submit UKTR response handling

Date: 2024-11-13

## Status

Accepted

## Context

We can receive 400 BAD_REQUEST responses from ETMP due to a malformed json request. 
However, given that we will be mirroring the request schema and validation done to this payload, the only realistic reason that we could get this response from 
ETMP would be due to an implementation error.

## Decision

We have decided that in the scenario that we receive a 400 BAD_REQUEST response from ETMP when submitting/amending UKTR's
we will return a 500 INTERNAL_SERVER_ERROR response to the consuming third party software.
We have also decided that we will transform the error details received from ETMP when returning any error responses to 
consuming third party software in order to match the errors that they might expect.

## Consequences

The openAPI specs for the public-facing create/amend UKTR submissions will differ slightly to the ETMP openAPI spec (EPID1516).
This could cause some initial confusion across services.
It also means that the information coming from ETMP may not reach the third party software in the exact same form.
