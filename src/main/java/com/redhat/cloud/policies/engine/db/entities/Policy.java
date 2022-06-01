package com.redhat.cloud.policies.engine.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "policy")
public class Policy {

    @Id
    public UUID id;

    @Column(name = "customerid")
    public String accountId;

    public String orgId;

    public String name;

    public String description;

    @Column(name = "conditions")
    public String condition;

    @Column(name = "is_enabled")
    public boolean enabled;

    public String actions;
}
