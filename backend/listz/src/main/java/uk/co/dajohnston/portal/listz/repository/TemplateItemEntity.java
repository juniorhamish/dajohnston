package uk.co.dajohnston.portal.listz.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listz_template_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @ManyToOne
  @JoinColumn(name = "template_id", nullable = false)
  private TemplateEntity template;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private ItemEntity item;

  @Column(name = "quantity_rule_type", nullable = false)
  private String quantityRuleType;

  @Column(name = "quantity_rule_value", nullable = false)
  private BigDecimal quantityRuleValue;

  @Column(name = "category_override")
  private String categoryOverride;
}
