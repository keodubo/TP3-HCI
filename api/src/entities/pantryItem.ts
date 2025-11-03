import { BaseEntity, Column, JoinColumn, CreateDateColumn, DeleteDateColumn, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn, UpdateDateColumn, Unique } from "typeorm";
import { IsOptional } from "class-validator";
import { User } from "./user";
import { Product } from "./product";
import { Pantry } from "./pantry";

@Entity()
export class PantryItem extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => Product, { nullable: false })
  @JoinColumn()
  product: Product;

  @Column({ nullable: false, type: 'float' })
  quantity: number;

  @Column({ nullable: true })
  unit: string;

  @Column({ type: "datetime", nullable: true })
  @IsOptional()
  expirationDate: Date | null;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @ManyToOne(() => User, user => user.pantryItems)
  owner: User;

  @ManyToOne(() => Pantry, pantry => pantry.items)
  pantry: Pantry;

  @Column({ nullable: true })
  addedAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  getFormattedListItem(): any {
    const product = this.product;
    const category = product?.category;
    return {
      id: String(this.id),
      product_id: product ? String(product.id) : null,
      product_name: product ? product.name : null,
      category_id: category ? String(category.id) : null,
      pantry_id: this.pantry ? String(this.pantry.id) : null,
      quantity: this.quantity,
      unit: this.unit,
      metadata: this.metadata ?? null,
      expiration_date: this.expirationDate ? this.expirationDate.toISOString() : null,
      created_at: this.createdAt?.toISOString() ?? null,
      updated_at: this.updatedAt?.toISOString() ?? null,
    };
  }
}
