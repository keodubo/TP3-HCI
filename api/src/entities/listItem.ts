import { BaseEntity, Column, JoinColumn, CreateDateColumn, DeleteDateColumn, Entity, ManyToOne, PrimaryGeneratedColumn, UpdateDateColumn } from "typeorm";
import { IsOptional } from "class-validator";
import { User } from "./user";
import { Product } from "./product";
import { List } from "./list";
import { Purchase } from "./purchase";

@Entity()
export class ListItem extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => Product, { nullable: false })
  @JoinColumn()
  product: Product;

  @Column({ nullable: false, type: 'float' })
  quantity: number;

  @Column({ nullable: true })
  unit: string;

  @Column({ default: false })
  purchased: boolean;

  @Column({ nullable: true })
  lastPurchasedAt: Date;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @ManyToOne(() => User, user => user.listItems)
  owner: User;

  @ManyToOne(() => List, list => list.items)
  list: List;

  @ManyToOne(() => Purchase, purchase => purchase.items)
  purchase: Purchase;

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  getFormattedListItem(): any {
    const productEntity = this.product;
    const category = productEntity?.category;
    const pantry = (productEntity as any)?.pantry;
    return {
      id: String(this.id),
      product_id: productEntity ? String(productEntity.id) : null,
      product_name: productEntity ? productEntity.name : null,
      category_id: category ? String(category.id) : null,
      pantry_id: pantry ? String(pantry.id) : null,
      quantity: this.quantity,
      unit: this.unit,
      metadata: this.metadata ?? null,
      purchased: this.purchased,
      is_acquired: this.purchased,
      last_purchased_at: this.lastPurchasedAt ? this.lastPurchasedAt.toISOString() : null,
      created_at: this.createdAt?.toISOString() ?? null,
      updated_at: this.updatedAt?.toISOString() ?? null,
    };
  }
}
