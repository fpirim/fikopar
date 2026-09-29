package models

import (
	"time"
)

type Role string

const (
	RoleAdmin Role = "admin"
	RoleUser  Role = "user"
)

type User struct {
	ID            uint           `gorm:"primaryKey" json:"id"`
	Email         string         `gorm:"uniqueIndex;not null" json:"email"`
	Password      string         `gorm:"not null" json:"-"`
	Name          string         `json:"name"`
	Role          Role           `gorm:"type:varchar(20);default:'user'" json:"role"`
	AvatarURL     *string        `json:"avatar_url"`
	Organizations []Organization `gorm:"many2many:user_organizations" json:"organizations,omitempty"`
	CreatedAt     time.Time      `json:"created_at"`
	UpdatedAt     time.Time      `json:"updated_at"`
}
