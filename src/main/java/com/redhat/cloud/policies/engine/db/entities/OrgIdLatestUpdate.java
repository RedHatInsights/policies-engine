package com.redhat.cloud.policies.engine.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_id_latest_update")
public class OrgIdLatestUpdate {

    @Id
    @Column(name = "org_id")
    public String orgId;

    public LocalDateTime latest;
}
