package com.redhat.cloud.policies.engine.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
