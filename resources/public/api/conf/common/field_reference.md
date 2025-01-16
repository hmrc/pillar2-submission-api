
This section contains information on the fields used in Pillar 2 API requests. Please use the glossary in the [service guide](https://developer.development.tax.service.gov.uk/guides/pillar2-service-guide/) if any terms in the reference are unfamiliar. 



<table>
<thead>
<tr>
<th>Field Name</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>accountingPeriodFrom</td>
<td>Start date of accounting period the group has registered with.</td>
</tr>
<tr>
<td>accountingPeriodTo</td>
<td>End date of accounting period  the group has registered with.</td>
</tr>
<tr>
<td>obligationMTT</td>
<td>Flag indicating whether group will be paying multinational topup tax.</td>
</tr>
<tr>
<td>electionUKGAAP</td>
<td>Only a domestic group can have a UK GAAP election. It allows a domestic group to use UK GAAP when calculating the amount of DTT due.</td>
</tr>
<tr>
<td>electionDTTSingleMember</td>
<td>Single member of the group who will submit returns and make payments on behalf of the entire group (only for DTT payments).</td>
</tr>
<tr>
<td>electionUTPRSingleMember</td>
<td>Single member of the group who will submit returns and make payments on behalf of the entire group (only for UTPR payments).</td>
</tr>
<tr>
<td>numberSubGroupDTT</td>
<td>Under clarification</td>
</tr>
<tr>
<td>numberSubGroupUTPR</td>
<td>Under clarification</td>
</tr>
<tr>
<td>totalLiabilityDTT</td>
<td>The overall total of tax due for entities with DTT liabilities.</td>
</tr>
<tr>
<td>totalLiabilityIIR</td>
<td>The overall total of tax due for entities with IIR liabilities.</td>
</tr>
<tr>
<td>totalLiabilityUTPR</td>
<td>The overall total of tax due for entities with UTPR liabilities.</td>
</tr>
<tr>
<td>ukChargeableEntityName</td>
<td>Name of the Entity liable for tax payments.</td>
</tr>
<tr>
<td>idType</td>
<td>Unique identifier type for the entity. CRN, UTR or TIN.</td>
</tr>
<tr>
<td>idValue</td>
<td>Unique Identifier for the entity.</td>
</tr>
<tr>
<td>amountOwedDTT</td>
<td>Amount of DTT owed by the entity.</td>
</tr>
<tr>
<td>amountOwedIIR</td>
<td>Amount of IIR owed by the entity.</td>
</tr>
<tr>
<td>amountOwedUTPR</td>
<td>Amount of UTPR owed by the entity.</td>
</tr>
<tr>
<td>returnType</td>
<td>Only supplied with Nil Return</td>
</tr>
</tbody>
</table>



