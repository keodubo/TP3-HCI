import {
  BaseEntity,
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn, RelationId,
  UpdateDateColumn
} from "typeorm";
import { User } from "./user";
import { List } from "./list";
import { ListItem } from "./listItem";

@Entity()
export class Purchase extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => List, list => list.purchaseHistory, { nullable: false })
  list: List;

  @RelationId((purchase: Purchase) => purchase.list)
  listId: number;

  @ManyToOne(() => User, owner => owner.purchases, { nullable: false })
  owner: User;

  @OneToMany(() => ListItem, item => item.purchase, { cascade: true })
  items: ListItem[];

  @Column({ type: "simple-json", nullable: true })
  metadata: Record<string, any>;

  @CreateDateColumn()
  createdAt: Date;

  @Column({ type: "datetime", nullable: true })
  restoredAt: Date | null;

  @DeleteDateColumn()
  deletedAt: Date;

  getFormattedPurchase(): any {
    return {
      id: String(this.id),
      list_id: this.list ? String(this.list.id) : String(this.listId),
      list: this.list ? this.list.getFormattedList() : null,
      purchased_at: this.createdAt?.toISOString() ?? null,
      restored_at: this.restoredAt ? this.restoredAt.toISOString() : null,
      metadata: this.metadata ?? null,
    };
  }
}
