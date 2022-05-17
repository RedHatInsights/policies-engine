package com.redhat.cloud.policies.engine.lightweight;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_latest_update")
public class AccountLatestUpdate {

    // TODO POL-650 shouldn't we use auto generated ids as PK instead of natural keys?
    @Id
    @Column(name = "account_id")
    public String accountId;

    @Column(name = "org_id")
    public String orgId;

    public LocalDateTime latest;
}
