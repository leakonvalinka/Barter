import {UserDetail} from './user';
import {Skill} from './skill';

/**
 * represents a Rating by a User
 */
export interface UserRating {
  id: number;
  ratingHalfStars: number;
  createdAt: string;

  title: string;
  description: string;

  /**
   * the following fields might not be set if their value is clear from the context
   */
  byUser: UserDetail | null;
  forUser: UserDetail | null;
  forSkill: Skill | null;
}

/**
 * represents the fields of UserRating necessary for creation
 */
export interface CreateUserRating {
  ratingHalfStars: number;

  title: string;
  description: string;
}
