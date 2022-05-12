package com.redhat.cloud.policies.engine.lightweight;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_latest_update")
public class AccountLatestUpdate {

    @Id
    @Column(name = "account_id")
    public String accountId;

    public LocalDateTime latest;
}
