package models

type UserOrganization struct {
	UserID         uint `gorm:"primaryKey"`
	OrganizationID uint `gorm:"primaryKey"`
}
