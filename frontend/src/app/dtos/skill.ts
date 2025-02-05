import {UserDetail} from './user';

export interface SkillBase {
  id: number;
  title: string;
  description: string;
  category: SkillCategory;
  byUser: UserDetail;
  type: 'demand' | 'offer'; // Discriminator
}

export interface SkillDemand extends SkillBase {
  type: 'demand';
  urgency: DemandUrgency;
  schedule?: string; // Reflects the schedule field in SkillOffer
}

export interface SkillOffer extends SkillBase {
  type: 'offer';
  schedule: string; // Reflects the schedule field in SkillOffer
  urgency?: DemandUrgency;
}

export enum DemandUrgency {
  NONE,
  LOW,
  MEDIUM,
  HIGH,
  CRITICAL
}

export interface CreateSkillCategory {
  id: number
}
export interface CreateSkillBase {
  title: string,
  description: string,
  category: CreateSkillCategory
}
export interface CreateDemand extends CreateSkillBase {
  urgency: DemandUrgency
}

export interface GeoPoint {
  type: string;
  coordinates: number[];
}

export interface UserLocation {
  homeLocation: GeoPoint;
}

export interface User {
  username: string;
  displayName: string;
  bio: string;
  profilePicture: string | null;
  location: UserLocation;
  createdAt: string;
}

export interface SkillDetail extends CreateSkillBase {
  id: number;
  byUser: User;
  schedule: string;
}

export interface CreateOffer extends CreateSkillBase {
  schedule: string
}

export interface UpdateSkillBase {
  id: number;
  title: string;
  description: string;
  category: CreateSkillCategory;
  byUser: UserDetail;
}

export interface UpdateSkillDemand extends UpdateSkillBase {
  urgency: DemandUrgency;
}

export interface UpdateSkillOffer extends UpdateSkillBase {
  schedule: string; // Reflects the schedule field in SkillOffer
}

export interface SkillCategory {
  id: number;
  name: string;
  description: string;
}
// Union type for polymorphic handling
export type Skill = SkillDemand | SkillOffer;
