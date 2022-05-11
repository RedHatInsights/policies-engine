package com.redhat.cloud.policies.engine.lightweight;

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

    @Column(name = "customerId")
    public String accountId;

    public String name;

    public String description;

    @Column(name = "conditions")
    public String condition;

    @Column(name = "is_enabled")
    public boolean enabled;

    public String actions;
}
