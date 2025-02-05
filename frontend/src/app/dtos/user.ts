import {SkillDemand, SkillOffer, GeoPoint} from './skill';

/**
 * Represents a User in the application.
 */
export interface User {
  id: string;
  email: string;
  password: string;
  username: string;
}

/**
 * represents a user's profile details
 */
export interface UserDetail {
  id: number;
  email: string;
  username: string;
  displayName: string;
  bio: string;
  /*
   * from client to server, this can be a Base64-encoded image
   * from server to client, this is an image UUID for retrieval via /images/uuid
  */
  profilePicture: string;
  location: UserAddress;
  skillDemands: SkillDemand[];
  skillOffers: SkillOffer[];

  averageRatingHalfStars?: number;
  numberOfRatings?: number;
}

/**
 * represents a user's updatable profile details, which they can modify
 */
export interface UserUpdate {
  displayName: string;
  bio: string;
  profilePicture: string | undefined;
  location: UserAddress;
}

export interface ChatUser {
  username: string;
  displayName: string;
  bio: string;
  profilePicture: string | undefined;
}

export interface UserAddress {
  street: string;
  streetNumber: string;
  city: string;
  postalCode: number;
  country: string;
  homeLocation: GeoPoint;
}

export interface VerificationRequest {
  email: string;
  verificationToken: string;
}
