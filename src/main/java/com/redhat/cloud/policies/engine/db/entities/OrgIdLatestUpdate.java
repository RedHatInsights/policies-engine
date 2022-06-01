package com.redhat.cloud.policies.engine.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_id_latest_update")
public class OrgIdLatestUpdate {

    @Id
    @Column(name = "org_id")
    public String orgId;

    public LocalDateTime latest;
}
