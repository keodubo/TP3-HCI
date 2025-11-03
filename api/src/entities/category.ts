import { BaseEntity, Column, CreateDateColumn, DeleteDateColumn, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn, UpdateDateColumn, Unique } from "typeorm";
import { IsOptional, Length } from "class-validator";
import { User } from "./user";
import { Product } from "./product";

@Entity()
@Unique(['name', 'owner'])
export class Category extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ nullable: false })
  @Length(1, 50)
  name: string;

  @Column({ type: "text", nullable: true })
  @IsOptional()
  description: string | null;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @ManyToOne(() => User, user => user.categories)
  owner: User;

  @OneToMany(() => Product, product => product.category)
  products: Product[];

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  getFormattedCategory(): any {
     return {
       id: String(this.id),
       name: this.name,
       description: this.description ?? null,
       metadata: this.metadata ?? null,
       created_at: this.createdAt?.toISOString() ?? null,
       updated_at: this.updatedAt?.toISOString() ?? null,
     }
   }
}
