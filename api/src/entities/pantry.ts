import {
  BaseEntity,
  Column,
  CreateDateColumn,
  DeleteDateColumn,
  Entity,
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
import {PantryItem} from "./pantryItem";
import {Product} from "./product";
import {removeUserForListShared} from "../utils/users";

@Entity()
@Unique(["name", "owner"])
export class Pantry extends BaseEntity {
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

  @ManyToOne(() => User, user => user.pantries)
  owner: User;

  @OneToMany(() => PantryItem, pantryItems => pantryItems.pantry)
  items: PantryItem[];

  @OneToMany(() => Product, product => product.pantry)
  products: Product[];

  @ManyToMany(() => User, user => user.sharedPantries)
  @JoinTable()
  sharedWith: User[];

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @DeleteDateColumn()
  @IsOptional()
  deletedAt: Date;

  getFormattedPantry(): any {
    const owner = this.owner?.getFormattedUser() ?? null;
    const sharedUsers = this.sharedWith?.map((user) => {
      const formatted = user.getFormattedUser();
      return {
        id: formatted.id,
        email: formatted.email,
        display_name: formatted.display_name,
        avatar: formatted.photo_url,
      };
    }) ?? [];
    const items = this.items ? this.items.map((item) => item.getFormattedListItem ? item.getFormattedListItem() : item) : [];

    return {
      id: String(this.id),
      name: this.name,
      description: this.description ?? null,
      metadata: this.metadata ?? null,
      owner_id: owner ? owner.id : null,
      owner,
      shared_users: sharedUsers,
      created_at: this.createdAt?.toISOString() ?? null,
      updated_at: this.updatedAt?.toISOString() ?? null,
      items,
    };
  }

}
