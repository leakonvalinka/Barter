export interface MapLocation {
    lat: number;
    lng: number;
    user: {
      displayName: string;
      username: string;
      profilePicture: string;
      rating: number;
      skills: string[];
    }
}
