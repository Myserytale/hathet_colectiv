# Business Analysis — Requirements, Use Cases & Activity Diagrams

**Assigned Role:** Business Analyst (BA)  
**Related Issues:** `ISS-02` (Sprint 1), `ISS-15` (Sprint 2), `ISS-26` (Sprint 4)

---

## 1. Actors (Baseline Scope)
- **Attendee:** Uses the React mobile web app to display a rotating signed QR code and receive geofenced flash discount alerts.
- **Bartender / POS Operator:** Uses a tablet in offline/low-connectivity mode to scan QR codes, verify signatures locally, queue transactions, and sync batches.
- **Event Organizer:** Monitors Uber H3 zone crowd occupancy and bottleneck alerts.
- **80k Agent Simulator:** Stress-tests crowd movement and payment batches.

---

## 2. Functional & Non-Functional Requirements
### 2.1. Functional Requirements

The following functional requirements describe the core behavior expected from PulseVenue. They represent an initial proposal.

| ID | Category | Summary | User Story | Acceptance Criteria |
|---|---|---|---|---|
| FR-01 | Wallet & QR | Generate rotating, signed QR payment tokens. | As an attendee, I want to display a QR token so that I can pay for purchases at the event. | 1. The token contains a wallet identifier, expiration time, and signature.<br>2. A new token can be requested when needed.<br>3. Invalid or expired tokens are rejected. |
| FR-02 | Offline POS | Validate QR tokens and record payments without internet access. | As a bartender, I want to record payments offline so that I can continue serving customers during network outages. | 1. The POS performs available local validation checks.<br>2. Provisionally accepted payments are saved locally with a unique ID.<br>3. Transactions are marked as pending synchronization. |
| FR-03 | Cloud Ledger | Synchronize offline transactions using a double-entry ledger. | As a POS operator, I want pending transactions to synchronize so that payment records are updated correctly. | 1. Pending transactions can be sent when connectivity returns.<br>2. Duplicate transaction IDs do not cause duplicate settlement.<br>3. The POS receives a synchronization result for each transaction. |
| FR-04 | Crowd Management | Detect congested zones and generate flash discounts. | As an event organizer, I want to detect overcrowded areas so that attendees can be encouraged to visit less crowded locations. | 1. Zones reaching the configured occupancy threshold are flagged.<br>2. Eligible alternative locations can receive promotional offers.<br>3. Demo values of 85% occupancy and 20% discount are subject to confirmation. |
| FR-05 | Wallet | Display wallet balance and payment information. | As an attendee, I want to check my wallet balance so that I can track my spending. | 1. The wallet displays the last known balance and currency.<br>2. Pending payments are distinguishable from settled payments.<br>3. Outdated balance information is identified when applicable. |
| FR-06 | POS Interface | Allow operators to enter amounts and view payment results. | As a bartender, I want clear payment feedback so that I know whether a purchase was recorded or rejected. | 1. The POS accepts valid positive payment amounts.<br>2. Invalid amounts are rejected.<br>3. The result clearly distinguishes provisional acceptance from final settlement. |
| FR-07 | Offline Queue | Display and manage locally pending transactions. | As a POS operator, I want to see pending transactions so that I can track payments awaiting synchronization. | 1. Pending transactions appear in a local list.<br>2. The list survives page refresh in the supported demo setup.<br>3. Transaction statuses update after synchronization. |
| FR-08 | Dashboard | Display zone occupancy and congestion alerts. | As an event organizer, I want to monitor zone occupancy so that I can identify crowded areas. | 1. The dashboard shows available zone counts and capacities.<br>2. Occupancy percentages are calculated.<br>3. Congested zones are visually highlighted. |
| FR-09 | Promotions | Display active location-based flash discounts. | As an attendee, I want to see available discounts so that I can choose less crowded bars. | 1. Active offers show the discount and destination.<br>2. Expired offers are not presented as active.<br>3. Eligibility follows the agreed zone rules. |
| FR-10 | Simulation | Simulate attendee activity and crowd congestion. | As a team member, I want to simulate crowd activity so that I can test congestion detection without a real event. | 1. The simulator generates occupancy updates for configured zones.<br>2. The backend can process generated data.<br>3. Advanced large-scale performance testing is treated as an optional later goal. |


### 2.2. Non-Functional Requirements

The following non-functional requirements define the initial quality expectations for the system. 

| ID | Category | Requirement | Acceptance Criteria |
|---|---|---|---|
| NFR-01 | Security | The system must protect payment tokens and sensitive payment-related data. | 1. Token-signing secrets are not exposed in the public frontend.<br>2. Invalid signatures are rejected.<br>3. Payment-related API traffic uses HTTPS in deployed environments. |
| NFR-02 | Offline Availability | The POS must support provisional payment recording during connectivity outages. | 1. Offline transactions can be saved without internet access.<br>2. Pending records remain available after a page refresh.<br>3. Synchronization can be initiated after reconnecting. |
| NFR-03 | Data Integrity | Transactions must be processed consistently without duplicate settlement. | 1. Repeated submissions with the same idempotency key do not create duplicate ledger entries.<br>2. Unresolved transactions retain a visible status. |
| NFR-04 | Performance | Core user interactions should respond within agreed performance limits. | 1. Proposed demo target: local POS validation completes within 2 seconds in at least 95% of test attempts.<br>2. The test environment and workload are documented. |
| NFR-05 | Scalability | The system should support simulated crowd workloads and allow performance evaluation. | 1. An initial crowd simulation can be executed and measured.<br>2. Performance metrics are collected for an agreed workload.<br>3. The 80,000-agent target is validated separately if included in the final scope. |


---


## 3. UML Use Case Diagram

The following diagram provides an overview of the main system actors and their interactions with PulseVenue. It is a Mermaid flowchart representation of the proposed use cases.

```mermaid

usecase-beta
direction LR

actor Attendee("Attendee")
actor Bartender("Bartender / POS Operator")
actor Organizer("Event Organizer")
actor Simulator("Crowd Simulator")

systemBoundary PulseVenue["PulseVenue System"]
    UC01("UC-01: View Wallet and QR Token")
    UC02("UC-02: Process Offline Payment")
    UC03("UC-03: Sync Pending Transactions")
    UC04("UC-04: Monitor Crowd Congestion")
    UC05("UC-05: View Flash Discounts")
    UC06("UC-06: Run Crowd Simulation")
    UC07("UC-07: Review Pending Transactions")
end

Attendee -- UC01
Attendee -- UC05

Bartender -- UC02
Bartender -- UC03
Bartender -- UC07

Organizer -- UC04

Simulator -- UC06
```


---

## 4. UML Activity Diagrams 

### 4.1. Offline QR Payment and Batch Synchronization
```mermaid
swimlane-beta LR

subgraph bartender [Bartender / POS Operator]
    start([Start])
    enter[Enter purchase amount]
    scan[Scan or paste QR token]
    result[View payment status]
end

subgraph pos [POS Application]
    verify[Verify token and offline rules]
    valid{Payment acceptable?}
    reject[Reject payment]
    save[Save transaction in local queue]
    online{Internet available?}
    pending[Keep transaction pending]
    send[Send transaction batch]
    update[Update local transaction status]
    finish([End])
end

subgraph backend [Backend and Ledger]
    check[Validate transaction and idempotency key]
    duplicate{Already processed?}
    previous[Return previous result]
    settle{Settlement valid?}
    ledger[Record debit and credit entries]
    failed[Return rejection status]
end

start --> enter --> scan --> verify
verify --> valid

valid -->|No| reject --> result
valid -->|Yes| save --> online

online -->|No| pending --> online
online -->|Yes| send --> check

check --> duplicate
duplicate -->|Yes| previous --> update
duplicate -->|No| settle
settle -->|Yes| ledger --> update
settle -->|No| failed --> update

update --> result
result --> finish
```


### 4.2. Crowd Congestion Detection and Flash Discounts
```mermaid
swimlane-beta LR
subgraph telemetry [GPS Telemetry / Simulator]
    start([Start])
    generate[Generate attendee location data]
    send[Send location updates]
end

subgraph backend [Crowd Management Service]
    aggregate[Aggregate attendees into H3 zones]
    occupancy[Calculate zone occupancy]
    congested{Occupancy above threshold?}
    find[Find less crowded bar]
    eligible{Eligible bar available?}
end

subgraph organizer [Event Organizer Dashboard]
    normal[Display normal occupancy]
    alert[Display congestion alert]
end

subgraph discounts [Discount Engine]
    active{Promotion already active?}
    keep[Keep existing promotion]
    create[Generate flash discount]
    store[Store discount and geofence]
    none[No new promotion]
end

subgraph attendee [Attendee Application]
    display[Display eligible flash discount]
    finish([End])
end

start --> generate --> send --> aggregate
aggregate --> occupancy --> congested

congested -->|No| normal --> finish
congested -->|Yes| alert --> find

find --> eligible
eligible -->|No| none --> finish
eligible -->|Yes| active

active -->|Yes| keep --> finish
active -->|No| create --> store --> display
display --> finish
```

---

## 5. Requirements Traceability Matrix (`TODO: ISS-26`)
> **Task for BA (`ISS-26` Pull Request):** Map each `FR-xx` to its Java service method, React component, and QA test case (`TC-xx`).
