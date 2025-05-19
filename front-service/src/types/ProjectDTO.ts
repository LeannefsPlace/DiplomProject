export interface ProjectDTO {
    id: number;
    name: string;
    description: string;
    createdAt: string;
    isActive: boolean;
    avatarUrl: string;
    projectMembers: ProjectMemberDTO[];
}

export interface ProjectMemberDTO{
    id:number;
    userId: number;
    role: ProjectRole;
}

enum ProjectRole{
    OWNER="OWNER", MEMBER="MEMBER", MANAGER="MANAGER"
}