import {
  BaseEntity,
  Column,
  Entity,
  OneToMany,
  OneToOne,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
  DeleteDateColumn,
  ManyToMany,
  CreateDateColumn,
  JoinTable
} from "typeorm";
import { IsEmail, IsOptional, Length } from "class-validator";
import { UserVerificationToken } from "./userVerificationToken";
import { UserPasswordRecoveryToken } from "./userPasswordRecoveryToken";
import { Category } from "./category";
import { Product } from "./product";
import { Pantry } from "./pantry";
import { ListItem } from "./listItem";
import { List } from "./list";
import { PantryItem } from "./pantryItem";
import { Purchase } from "./purchase";

@Entity()
export class User extends BaseEntity {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ nullable: false })
  @Length(1, 100)
  displayName: string;

  @Column({ nullable: false, unique: true })
  @IsEmail()
  email: string;

  @Column({ nullable: true })
  @IsOptional()
  photoUrl: string;

  @Column({ type: "simple-json", nullable: true })
  @IsOptional()
  metadata: Record<string, any>;

  @Column({ type: "text", nullable: true })
  @IsOptional()
  bio: string | null;

  @Column({ nullable: true })
  @IsOptional()
  phoneNumber: string | null;

  @Column({ nullable: true })
  @IsOptional()
  preferredLanguage: string | null;

  @Column({ nullable: false, default: true })
  notificationOptIn: boolean;

  @Column({ nullable: false, default: 'system' })
  themeMode: string;

  getFormattedUser(): any {
    return {
      id: String(this.id),
      email: this.email,
      display_name: this.displayName,
      photo_url: this.photoUrl ?? null,
      is_verified: this.isVerified,
      metadata: this.metadata ?? {},
    };
  }

  getSummary(): any {
    return {
      id: String(this.id),
      email: this.email,
      display_name: this.displayName,
      avatar: this.photoUrl ?? null,
    };
  }

  @Column({nullable: false, select: false})
  password: string;

  @Column({ nullable: false, default: false })
  isVerified: boolean;

  @DeleteDateColumn({ select: false })
  @IsOptional()
  deletedAt: Date;

  @OneToOne(() => UserVerificationToken, verificationToken => verificationToken.user)
  verificationToken: UserVerificationToken;

  @OneToOne(() => UserPasswordRecoveryToken, passwordRecoveryToken => passwordRecoveryToken.user)
  passwordRecoveryToken: UserPasswordRecoveryToken;

  @OneToMany(() => Category, category => category.owner)
  categories: Category[];

  @OneToMany(() => Pantry, pantry => pantry.owner)
  pantries: Pantry[];

  @OneToMany(() => Product, product => product.owner)
  products: Product[];

  @OneToMany(() => List, list => list.owner)
  lists: List[];

  @OneToMany(() => ListItem, listItem => listItem.owner)
  listItems: ListItem[];

  @OneToMany(() => PantryItem, pantryItem => pantryItem.owner)
  pantryItems: PantryItem[];

  @OneToMany(() => Purchase, purchase => purchase.owner)
  purchases: Purchase[];

  @ManyToMany(() => List, list => list.sharedWith)
  sharedLists: List[];

  @ManyToMany(() => Pantry, pantry => pantry.sharedWith)
  sharedPantries: Pantry[];

  @UpdateDateColumn()
  updatedAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  getProfile(): any {
    return {
      user_id: String(this.id),
      bio: this.bio ?? null,
      phone_number: this.phoneNumber ?? null,
      preferred_language: this.preferredLanguage ?? null,
      notification_opt_in: this.notificationOptIn,
      theme_mode: this.themeMode,
      updated_at: this.updatedAt.toISOString(),
    };
  }
}
