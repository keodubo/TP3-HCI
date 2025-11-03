import {
  BaseEntity,
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
  JoinColumn,
  JoinTable,
  ManyToMany,
  ManyToOne,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
  Unique
} from "typeorm";
import {IsOptional, Length} from "class-validator";
import {User} from "./user";
import {ListItem} from "./listItem";
import {Purchase} from "./purchase";

@Entity()
@Unique("unique_list_name_per_owner", ["name", "owner"])
export class List extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ nullable: false })
  @Length(1, 50)
  name: string;

  @Column({ nullable: true })
  @Length(0, 200)
  description: string | null;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @Column({ nullable: false, default: false })
  recurring: boolean;

  @ManyToOne(() => User, user => user.lists)
  owner: User;

  @OneToMany(() => ListItem, listItem => listItem.list)
  items: ListItem[];

  @ManyToMany(() => User, user => user.sharedLists)
  @JoinTable()
  sharedWith: User[];

  @OneToMany(() => Purchase, purchase => purchase.list)
  @JoinColumn()
  purchaseHistory: Purchase[];

  @UpdateDateColumn()
  updatedAt: Date;

  @Column({type: "date", nullable: true})
  @IsOptional()
  lastPurchasedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  formatDate(date: any): string | null {
    if (!date) return null;
    if (date instanceof Date) return date.toISOString();
    if (typeof date === "string") {
      const d = new Date(date);
      if (!isNaN(d.getTime())) return d.toISOString();
    }
    return null;
  }

  getFormattedList(): any {
    const owner = this.owner?.getSummary() ?? null;
    const sharedUsers = this.sharedWith
      ? this.sharedWith.map(user => {
          if (typeof user.getSummary === 'function') {
            return user.getSummary();
          }
          const formatted = (user as any).getFormattedUser ? (user as any).getFormattedUser() : {
            id: String(user.id),
            email: user.email,
            display_name: (user as any).displayName ?? null,
            avatar: (user as any).photoUrl ?? null,
          };
          return formatted;
        })
      : [];

    const items = this.items ? this.items.map(item => item.getFormattedListItem()) : [];

    return {
      id: String(this.id),
      name: this.name,
      description: this.description ?? null,
      owner_id: owner ? owner.id : null,
      owner,
      shared_users: sharedUsers,
      metadata: this.metadata ?? null,
      is_recurring: this.recurring,
      recurring: this.recurring,
      created_at: this.formatDate(this.createdAt),
      updated_at: this.formatDate(this.updatedAt),
      last_purchased_at: this.formatDate(this.lastPurchasedAt),
      items,
    }
  }
}
