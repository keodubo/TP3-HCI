import {
  BaseEntity,
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
  Unique
} from "typeorm";
import {IsOptional, Length} from "class-validator";
import {User} from "./user";
import {Category} from "./category";
import {Pantry} from "./pantry";

@Entity()
@Unique(['name', 'owner'])
export class Product extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({nullable: false})
  @Length(1, 50)
  name: string;

  @Column({ type: "text", nullable: true })
  @IsOptional()
  description: string | null;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @Column({ nullable: true })
  @IsOptional()
  unit: string | null;

  @Column({ type: "float", default: 1 })
  defaultQuantity: number;

  @Column({ nullable: false, default: false })
  isFavorite: boolean;

  @ManyToOne(() => User, user => user.products)
  owner: User;

  @ManyToOne(() => Category, category => category.products)
  @IsOptional()
  category: Category;

  @ManyToOne(() => Pantry, pantry => pantry.products)
  @IsOptional()
  pantry: Pantry;

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  getFormattedProduct(): any {
    return {
      id: String(this.id),
      name: this.name,
      description: this.description ?? null,
      category_id: this.category ? String(this.category.id) : null,
      category: this.category?.getFormattedCategory() ?? null,
      metadata: this.metadata ?? null,
      unit: this.unit ?? null,
      default_quantity: this.defaultQuantity ?? 1,
      is_favorite: this.isFavorite ?? false,
      created_at: this.createdAt?.toISOString() ?? null,
      updated_at: this.updatedAt?.toISOString() ?? null,
    };
  }
}
